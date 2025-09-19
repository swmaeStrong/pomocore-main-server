package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.config.s3.S3Properties;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.common.util.badWords.BadWordsFilter;
import com.swmStrong.demo.domain.streak.entity.Streak;
import com.swmStrong.demo.domain.streak.facade.StreakProvider;
import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import com.swmStrong.demo.infra.token.TokenManager;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final RedisRepository redisRepository;
    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final StreakProvider streakProvider;
    private final ApplicationContext applicationContext;

    public static final String REGISTERED_IP_COUNT_FORMAT = "registerIpCount:%s" ;
    public static final String USER_ONLINE_FORMAT = "userOnline:%s";
    public static final String USER_INFO_FORMAT = "userInfo:%s";

    private static final int USER_INFO_EXPIRES = 3600; // 1 hour
    private static final int USER_ONLINE_EXPIRES = 86400; // 1 day

    public UserServiceImpl(
            UserRepository userRepository,
            TokenManager tokenManager,
            RedisRepository redisRepository,
            StreakProvider streakProvider,
            S3Client s3Client,
            S3Properties s3Properties,
            ApplicationContext applicationContext
    ) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
        this.redisRepository = redisRepository;
        this.streakProvider = streakProvider;
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
        this.applicationContext = applicationContext;
    }

    @Override
    public UserInfoResponseDto getDetailsByUserId(String userId) {
        UserResponseDto userResponseDto = getInfoById(userId);

        Streak streak = streakProvider.loadStreakByUserId(userId);

        int currentStreak = streak == null ? 0: streak.getCurrentStreak();
        int maxStreak = streak == null ? 0: streak.getMaxStreak();
        Integer totalSession = streakProvider.loadTotalSessionByUserId(userId);

        return UserInfoResponseDto.builder()
                .userId(userId)
                .nickname(userResponseDto.nickname())
                .profileImageUrl(userResponseDto.profileImageUrl())
                .currentStreak(currentStreak)
                .maxStreak(maxStreak)
                .totalSession(totalSession == null ? 0 : totalSession)
                .build();
    }

    @Override
    public TokenResponseDto signupGuest(HttpServletRequest request, UserRequestDto userRequestDto) {
        if (userRepository.existsById(userRequestDto.userId())) {
            throw new ApiException(ErrorCode.DUPLICATE_USER_ID);
        }

        String requestIP = request.getHeader("X-Forwarded-For");
        if (requestIP == null) {
            requestIP = request.getRemoteAddr();
        }

        Long count = redisRepository.incrementWithExpireIfFirst(getRegisterCountKey(requestIP), 1, TimeUnit.HOURS);
        if (count > 5) {
            throw new ApiException(ErrorCode.IP_RATE_LIMIT_EXCEEDED);
        }
        User user = userRepository.save(User.of(userRequestDto));
        saveUserInfoCache(user);
        return tokenManager.getToken(user.getId(), request.getHeader("User-Agent"), Role.UNREGISTERED);
    }

    @Override
    public void validateNickname(String userId, String nickname) {
        if (BadWordsFilter.isBadWord(nickname)) {
            throw new ApiException(ErrorCode.BAD_WORD_FILTER);
        }

        String currentNickname = getInfoById(userId).nickname();
        if (currentNickname==null || !currentNickname.equals(nickname)) {
            if (userRepository.existsByNickname(nickname)) {
                throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
            }
        }
    }

    @Override
    public UserResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto) {
        validateNickname(userId, nicknameRequestDto.nickname());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        user.updateNickname(nicknameRequestDto.nickname());
        user = userRepository.save(user);

        return saveUserInfoCache(user);
    }

    @Override
    public UserResponseDto getInfoByIdOrNickname(String userId, String nickname) {
        boolean isUserIdExists = userId != null && !userId.isEmpty();
        boolean isNicknameExists = nickname != null && !nickname.isEmpty();
        if (isUserIdExists ^ isNicknameExists) {
            if (isUserIdExists) {
                return getInfoById(userId);
            } else {
                return getInfoByNickname(nickname);
            }
        }
        throw new ApiException(ErrorCode._BAD_REQUEST);
    }

    @Override
    public UserResponseDto getInfoById(String userId) {
        String key = getUserInfoKey(userId);
        UserResponseDto userResponseDto = redisRepository.getJsonData(key, UserResponseDto.class);
        if (userResponseDto == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
            userResponseDto = saveUserInfoCache(user);
        }
        return userResponseDto;
    }

    @Override
    public UserResponseDto getInfoByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new ApiException(ErrorCode.NICKNAME_NOT_FOUND));

        return UserResponseDto.of(user);
    }

    @Override
    public void deleteUserById(String userId) {
        userRepository.deleteById(userId);
        redisRepository.deleteData(getUserInfoKey(userId));
    }

    @Override
    public String uploadProfileImage(String userId, MultipartFile file) {
        validateFile(file);

        String tempS3Key = uploadToS3(userId, file);
        String imageUrl = generateImageUrl(tempS3Key);

        try {
            UserServiceImpl proxy = applicationContext.getBean(UserServiceImpl.class);
            String oldImageKey = proxy.updateProfileImageInTransaction(userId, imageUrl, tempS3Key);

            if (oldImageKey != null && !oldImageKey.isEmpty()) {
                deleteS3ImageAsync(oldImageKey);
            }
            log.debug("Profile image uploaded successfully for user: {}, key: {}", userId, tempS3Key);
            return imageUrl;

        } catch (Exception e) {
            deleteS3ImageAsync(tempS3Key);
            log.error("DB update failed, rolling back S3 upload for user: {}", userId, e);
            throw e;
        }
    }

    @Override
    public void deleteProfileImage(String userId) {
        // Phase 1: DB에서 이미지 키 조회 및 초기화 (트랜잭션 내)
        UserServiceImpl proxy = applicationContext.getBean(UserServiceImpl.class);
        String imageKeyToDelete = proxy.clearProfileImageInTransaction(userId);

        // Phase 2: S3에서 이미지 삭제 (비동기)
        if (imageKeyToDelete != null && !imageKeyToDelete.isEmpty()) {
            deleteS3ImageAsync(imageKeyToDelete);
            log.debug("Profile image deletion initiated for user: {}", userId);
        } else {
            log.warn("No profile image to delete for user: {}", userId);
        }
    }

    @Override
    public void goOnline(String userId, OnlineRequestDto onlineRequestDto) {
        String key = getUserOnlineKey(userId);
        redisRepository.setJsonDataWithExpire(key, onlineRequestDto, USER_ONLINE_EXPIRES);
    }

    @Override
    public Map<String, OnlineRequestDto> getUserOnlineDetails(List<String> userIds) {
        List<String> keys = userIds.stream()
                .map(this::getUserOnlineKey)
                .toList();

        Map<String, OnlineRequestDto> keysToOnlineDetails = redisRepository.multiGetJson(keys, OnlineRequestDto.class);
        Map<String, OnlineRequestDto> userOnlineDetails = new HashMap<>();

        for (String userId : userIds) {
            String key = getUserOnlineKey(userId);
            OnlineRequestDto onlineData = keysToOnlineDetails.get(key);
            userOnlineDetails.put(userId, onlineData);
        }

        return userOnlineDetails;
    }

    @Override
    public void dropOut(String userId) {
        String key = getUserOnlineKey(userId);
        OnlineRequestDto onlineRequestDto = new OnlineRequestDto((double) System.currentTimeMillis() /1000, 0 );
        redisRepository.setJsonDataWithExpire(key, onlineRequestDto, USER_ONLINE_EXPIRES);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_FILE);
        }

        if (file.getSize() > s3Properties.maxFileSize()) {
            throw new ApiException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !s3Properties.allowedContentTypes().contains(contentType)) {
            throw new ApiException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String generateFileName(String userId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return userId + "_" + UUID.randomUUID().toString() + extension;
    }

    private String generateImageUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.bucketName(),
                s3Properties.region(),
                s3Key);
    }


    private String getRegisterCountKey(String requestIP) {
        return String.format(REGISTERED_IP_COUNT_FORMAT, requestIP);
    }

    private String getUserOnlineKey(String userId) {
        return String.format(USER_ONLINE_FORMAT, userId);
    }

    private String getUserInfoKey(String userId) {
        return String.format(USER_INFO_FORMAT, userId);
    }


    private String uploadToS3(String userId, MultipartFile file) {
        String fileName = generateFileName(userId, file);
        String s3Key = s3Properties.profileImagePath() + fileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.bucketName())
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.debug("Profile image uploaded to S3: {}", s3Key);
            return s3Key;

        } catch (IOException e) {
            log.error("Failed to upload profile image to S3 for user: {}", userId, e);
            throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
        } catch (S3Exception e) {
            log.error("S3 error while uploading profile image for user: {}", userId, e);
            throw new ApiException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }

    @Transactional
    protected String updateProfileImageInTransaction(String userId, String imageUrl, String s3Key) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        String oldImageKey = user.getProfileImageKey();
        user.updateProfileImage(imageUrl, s3Key);
        userRepository.save(user);
        saveUserInfoCache(user);

        return oldImageKey;
    }

    @Transactional
    protected String clearProfileImageInTransaction(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        String imageKeyToDelete = user.getProfileImageKey();
        user.updateProfileImage(null, null);
        userRepository.save(user);
        saveUserInfoCache(user);

        return imageKeyToDelete;
    }

    @Async
    protected void deleteS3ImageAsync(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.bucketName())
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.debug("S3 image deleted asynchronously: {}", s3Key);

        } catch (S3Exception e) {
            log.warn("Failed to delete S3 image asynchronously: {}", s3Key, e);
        }
    }

    private UserResponseDto saveUserInfoCache(User user) {
        String key = getUserInfoKey(user.getId());
        UserResponseDto userResponseDto = UserResponseDto.of(user);
        redisRepository.setJsonDataWithExpire(key, userResponseDto, USER_INFO_EXPIRES);

        return userResponseDto;
    }
}

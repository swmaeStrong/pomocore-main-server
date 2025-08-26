package com.swmStrong.demo.domain.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Service;
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
    private final ObjectMapper objectMapper;
    private final StreakProvider streakProvider;

    public static final String REGISTER_IP_COUNT_PREFIX = "registerIpCount:";
    public static final String USER_ONLINE_PREFIX = "userOnline";
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
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
        this.redisRepository = redisRepository;
        this.streakProvider = streakProvider;
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public UserInfoResponseDto getDetailsByUserId(String userId) {
        String nickname, profileImageUrl;

        String key = String.format(USER_INFO_FORMAT, userId);
        UserResponseDto userResponseDto = redisRepository.getJsonData(key, UserResponseDto.class);
        if (userResponseDto != null) {
            nickname = userResponseDto.nickname();
            profileImageUrl = userResponseDto.profileImageUrl();
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

            nickname = user.getNickname();
            profileImageUrl = user.getProfileImageUrl();
        }

        Streak streak = streakProvider.loadStreakByUserId(userId);

        int currentStreak = streak == null ? 0: streak.getCurrentStreak();
        int maxStreak = streak == null ? 0: streak.getMaxStreak();
        Integer totalSession = streakProvider.loadTotalSessionByUserId(userId);

        return UserInfoResponseDto.builder()
                .userId(userId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
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
        return tokenManager.getToken(user.getId(), request.getHeader("User-Agent"), Role.UNREGISTERED);
    }

    @Override
    public void validateNickname(String userId, String nickname) {
        if (BadWordsFilter.isBadWord(nickname)) {
            throw new ApiException(ErrorCode.BAD_WORD_FILTER);
        }

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user != null && user.getNickname() != null && user.getNickname().equals(nickname)) {
            return;
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    @Override
    public UserResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto) {
        validateNickname(userId, nicknameRequestDto.nickname());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        user.updateNickname(nicknameRequestDto.nickname());

        user = userRepository.save(user);

        String key = getUserInfoKey(userId);
        UserResponseDto userResponseDto = UserResponseDto.of(user);
        redisRepository.setJsonDataWithExpire(key, userResponseDto, USER_INFO_EXPIRES);

        return userResponseDto;
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

        if (userResponseDto != null) {
            log.trace("Return user info by cache");
            return userResponseDto;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        userResponseDto = UserResponseDto.of(user);
        redisRepository.setJsonDataWithExpire(key, userResponseDto, USER_INFO_EXPIRES);
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
        if (!userRepository.existsById(userId)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);
    }

    @Override
    public String uploadProfileImage(String userId, MultipartFile file) {
        validateFile(file);

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

            String imageUrl = generateImageUrl(s3Key);
            
            updateUserProfileImage(userId, imageUrl, s3Key);
            
            log.info("Profile image uploaded successfully for user: {}, key: {}", userId, s3Key);
            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to upload profile image for user: {}", userId, e);
            throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
        } catch (S3Exception e) {
            log.error("S3 error while uploading profile image for user: {}", userId, e);
            throw new ApiException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }

    @Override
    public void deleteProfileImage(String userId) {
        try {
            User user = getUserEntityById(userId);
            String profileImageKey = user.getProfileImageKey();
            
            if (profileImageKey == null || profileImageKey.isEmpty()) {
                log.warn("No profile image to delete for user: {}", userId);
                return;
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.bucketName())
                    .key(profileImageKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            
            updateUserProfileImage(userId, null, null);
            
            log.info("Profile image deleted successfully for user: {}", userId);

        } catch (S3Exception e) {
            log.error("S3 error while deleting profile image for user: {}", userId, e);
            throw new ApiException(ErrorCode.EXTERNAL_SERVICE_ERROR);
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
    public Map<String, Double> getUserOnline(List<String> userIds) {
        Map<String, OnlineRequestDto> userOnlineDetails = getUserOnlineDetails(userIds);
        Map<String, Double> userLastActivities = new HashMap<>();
        
        for (String userId : userIds) {
            OnlineRequestDto onlineRequestDto = userOnlineDetails.get(userId);
            if (onlineRequestDto != null) {
                double lastActivityTime = onlineRequestDto.timestamp() + onlineRequestDto.sessionMinutes() * 60.0;
                userLastActivities.put(userId, lastActivityTime);
            } else {
                userLastActivities.put(userId, 0.0); // 미접속 처리
            }
        }
        
        return userLastActivities;
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

    private void updateUserProfileImage(String userId, String profileImageUrl, String profileImageKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        
        user.updateProfileImage(profileImageUrl, profileImageKey);
        userRepository.save(user);
    }

    private User getUserEntityById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    private String getRegisterCountKey(String requestIP) {
        return REGISTER_IP_COUNT_PREFIX+requestIP;
    }

    private String getUserOnlineKey(String userId) {
        return String.format("%s:%s", USER_ONLINE_PREFIX, userId);
    }

    private String getUserInfoKey(String userId) {
        return String.format(USER_INFO_FORMAT, userId);
    }
}

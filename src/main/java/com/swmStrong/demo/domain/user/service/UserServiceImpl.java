package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.config.s3.S3Properties;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.common.util.badWords.BadWordsFilter;
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

    public static final String REGISTER_IP_COUNT_PREFIX = "registerIpCount:";

    public UserServiceImpl(
            UserRepository userRepository,
            TokenManager tokenManager,
            RedisRepository redisRepository,
            S3Client s3Client,
            S3Properties s3Properties
    ) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
        this.redisRepository = redisRepository;
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
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

        Long count = redisRepository.incrementWithExpireIfFirst(getKey(requestIP), 1, TimeUnit.HOURS);
        if (count > 5) {
            throw new ApiException(ErrorCode.IP_RATE_LIMIT_EXCEEDED);
        }

        User user = userRepository.save(User.of(userRequestDto));
        return tokenManager.getToken(user.getId(), request.getHeader("User-Agent"), Role.UNREGISTERED);
    }

    @Override
    public void validateNickname(String nickname) {
        if (BadWordsFilter.isBadWord(nickname)) {
            throw new ApiException(ErrorCode.BAD_WORD_FILTER);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    @Override
    public UserResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        validateNickname(user.getNickname());

        user.updateNickname(nicknameRequestDto.nickname());
        userRepository.save(user);
        return UserResponseDto.of(user);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return UserResponseDto.of(user);
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

    private String getKey(String requestIP) {
        return REGISTER_IP_COUNT_PREFIX+requestIP;
    }
}

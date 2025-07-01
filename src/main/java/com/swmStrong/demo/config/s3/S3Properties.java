package com.swmStrong.demo.config.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        String bucketName,
        @DefaultValue("ap-northeast-2") String region,
        @DefaultValue("profile-images/") String profileImagePath,
        @DefaultValue("5242880") long maxFileSize,
        @DefaultValue("image/jpeg,image/jpg,image/png,image/gif,image/webp") List<String> allowedContentTypes
) {
}

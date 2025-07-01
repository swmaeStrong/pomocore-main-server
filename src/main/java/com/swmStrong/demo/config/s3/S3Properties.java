package com.swmStrong.demo.config.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        String bucketName,
        String region,
        String profileImagePath,
        long maxFileSize,
        List<String> allowedContentTypes
) {
}

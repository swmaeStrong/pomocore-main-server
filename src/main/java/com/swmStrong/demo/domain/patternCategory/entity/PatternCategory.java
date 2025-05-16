package com.swmStrong.demo.domain.patternCategory.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PatternCategory {
    @Id
    private String id;

    private String category;
    private List<String> patterns;
}
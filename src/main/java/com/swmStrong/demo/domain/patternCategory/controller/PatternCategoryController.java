package com.swmStrong.demo.domain.patternCategory.controller;

import com.swmStrong.demo.domain.patternCategory.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.patternCategory.dto.PatternRequestDto;
import com.swmStrong.demo.domain.patternCategory.service.PatternCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pattern")
public class PatternCategoryController {

    private final PatternCategoryService patternCategoryService;

    public PatternCategoryController(PatternCategoryService patternCategoryService) {
        this.patternCategoryService = patternCategoryService;
    }

    @PostMapping
    public ResponseEntity<Void> addPattern(@RequestBody PatternRequestDto patternRequestDto) {
        patternCategoryService.addPattern(patternRequestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePatternByCategoryAndPattern(@RequestBody PatternRequestDto patternRequestDto) {
        patternCategoryService.deletePatternByCategory(patternRequestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/category")
    public ResponseEntity<Void> deletePatternByCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
        patternCategoryService.deleteCategory(categoryRequestDto.category());
        return ResponseEntity.ok().build();
    }
}

package com.swmStrong.demo.domain.patternCategory.controller;

import com.swmStrong.demo.domain.patternCategory.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.patternCategory.dto.PatternRequestDto;
import com.swmStrong.demo.domain.patternCategory.service.PatternCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//TODO: 계층 구조는 {카테고리}/{패턴} 이기 때문에 계층 구조 생각해서 다시 생각해보기
@Tag(name = "카테고리-패턴")
@RestController
@RequestMapping("/pattern")
public class PatternCategoryController {

    private final PatternCategoryService patternCategoryService;

    public PatternCategoryController(PatternCategoryService patternCategoryService) {
        this.patternCategoryService = patternCategoryService;
    }

    @Operation(
            summary = "패턴 추가",
            description =
                "<p> 패턴을 추가한다. </p>" +
                "<p> 추가하려는 카테고리가 없다면 자동 생성된다.</p>"
    )
    @PostMapping
    public ResponseEntity<Void> addPattern(@RequestBody PatternRequestDto patternRequestDto) {
        patternCategoryService.addPattern(patternRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "패턴 삭제",
            description = "<p> 패턴을 삭제한다. </p>"
    )
    @DeleteMapping
    public ResponseEntity<Void> deletePatternByCategoryAndPattern(@RequestBody PatternRequestDto patternRequestDto) {
        patternCategoryService.deletePatternByCategory(patternRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "카테고리 전체 삭제",
            description =
                "<p> 카테고리 전체를 삭제한다. </p>" +
                "<p> 카테고리에 포함된 패턴도 전부 삭제된다. </p>"
    )
    @DeleteMapping("/category")
    public ResponseEntity<Void> deletePatternByCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
        patternCategoryService.deleteCategory(categoryRequestDto.category());
        return ResponseEntity.ok().build();
    }
}

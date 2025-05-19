package com.swmStrong.demo.domain.categoryPattern.controller;

import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;
import com.swmStrong.demo.domain.categoryPattern.service.CategoryPatternService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//TODO: 계층 구조는 {카테고리}/{패턴} 이기 때문에 계층 구조 생각해서 다시 생각해보기
@Tag(name = "카테고리-패턴")
@RestController
@RequestMapping("/category")
public class CategoryPatternController {

    private final CategoryPatternService categoryPatternService;

    public CategoryPatternController(CategoryPatternService categoryPatternService) {
        this.categoryPatternService = categoryPatternService;
    }

    @Operation(
            summary = "패턴 추가",
            description =
                "<p> 패턴을 추가한다. </p>" +
                "<p> 추가하려는 카테고리가 없다면 자동 생성된다.</p>"
    )
    @PostMapping("/{category}")
    public ResponseEntity<Void> addPattern(@PathVariable String category, @RequestBody PatternRequestDto patternRequestDto) {
        categoryPatternService.addPattern(category, patternRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "패턴 삭제",
            description = "<p> 패턴을 삭제한다. </p>"
    )
    @DeleteMapping("/{category}/pattern")
    public ResponseEntity<Void> deletePatternByCategoryAndPattern(@PathVariable String category, @RequestBody PatternRequestDto patternRequestDto) {
        categoryPatternService.deletePatternByCategory(category, patternRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "카테고리 전체 삭제",
            description =
                "<p> 카테고리 전체를 삭제한다. </p>" +
                "<p> 카테고리에 포함된 패턴도 전부 삭제된다. </p>"
    )
    @DeleteMapping("/{category}")
    public ResponseEntity<Void> deletePatternByCategory(@PathVariable String category) {
        categoryPatternService.deleteCategory(category);
        return ResponseEntity.ok().build();
    }
}

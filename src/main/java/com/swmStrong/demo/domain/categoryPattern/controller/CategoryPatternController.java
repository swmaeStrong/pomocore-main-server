package com.swmStrong.demo.domain.categoryPattern.controller;

import com.swmStrong.demo.domain.categoryPattern.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryResponseDto;
import com.swmStrong.demo.domain.categoryPattern.dto.ColorRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;
import com.swmStrong.demo.domain.categoryPattern.service.CategoryPatternService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                "<p> 카테고리에 해당하는 패턴을 추가한다. </p>"
    )
    @PostMapping("/{category}")
    public ResponseEntity<Void> addPattern(@PathVariable String category, @RequestBody PatternRequestDto patternRequestDto) {
        categoryPatternService.addPattern(category, patternRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "패턴 삭제",
            description = "<p> 카테고리 안에 있는 패턴을 삭제한다. </p>"
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

    @Operation(
            summary = "해당 카테고리 조회",
            description =
                "<p> 카테고리 이름에 맞는 카테고리를 조회한다. </p>" +
                "<p> 카테고리 내부의 패턴도 함께 출력된다. </p>"
    )
    @GetMapping("/{category}")
    public ResponseEntity<CategoryResponseDto> getCategoryByCategory(@PathVariable String category) {
        return ResponseEntity.ok(categoryPatternService.getCategoryByCategory(category));
    }

    @Operation(
            summary = "카테고리 전체 조회",
            description =
                "<p> 모든 카테고리를 조회한다. </p>" +
                "<p> 카테고리 내부의 패턴도 함께 출력된다. </p>"
    )
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryPatternService.getCategories());
    }

    @Operation(
            summary = "카테고리 색깔 수정",
            description =
                "<p> 카테고리의 색깔을 수정한다. </p>" +
                "<p> 색깔은 #000000 ~ #FFFFFF 로 입력한다. </p>"
    )
    @PatchMapping("/{category}/color")
    public ResponseEntity<Void> updateCategoryColor(@PathVariable String category, @RequestBody ColorRequestDto colorRequestDto) {
        categoryPatternService.setCategoryColor(category, colorRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "카테고리 추가",
            description =
                "<p> 카테고리를 추가한다. </p>"
    )
    @PostMapping
    public ResponseEntity<Void> addCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
        categoryPatternService.addCategory(categoryRequestDto);
        return ResponseEntity.ok().build();
    }
}

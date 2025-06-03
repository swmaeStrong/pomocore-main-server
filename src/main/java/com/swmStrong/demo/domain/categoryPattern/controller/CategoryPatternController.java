package com.swmStrong.demo.domain.categoryPattern.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryResponseDto;
import com.swmStrong.demo.domain.categoryPattern.dto.UpdateCategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;
import com.swmStrong.demo.domain.categoryPattern.service.CategoryPatternService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "패턴 추가",
            description =
                "<p> 카테고리에 해당하는 패턴을 추가한다. </p>"
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{category}")
    public ResponseEntity<ApiResponse<Void>> addPattern(@PathVariable String category, @RequestBody PatternRequestDto patternRequestDto) {
        categoryPatternService.addPattern(category, patternRequestDto);
        return CustomResponseEntity.of(SuccessCode._OK);
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "패턴 삭제",
            description = "<p> 카테고리 안에 있는 패턴을 삭제한다. </p>"
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{category}/pattern")
    public ResponseEntity<ApiResponse<Void>> deletePatternByCategoryAndPattern(@PathVariable String category, @RequestBody PatternRequestDto patternRequestDto) {
        categoryPatternService.deletePatternByCategory(category, patternRequestDto);
        return CustomResponseEntity.of(SuccessCode._NO_CONTENT);
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "카테고리 전체 삭제",
            description =
                "<p> 카테고리 전체를 삭제한다. </p>" +
                "<p> 카테고리에 포함된 패턴도 전부 삭제된다. </p>"
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{category}")
    public ResponseEntity<ApiResponse<Void>> deletePatternByCategory(@PathVariable String category) {
        categoryPatternService.deleteCategory(category);
        return CustomResponseEntity.of(SuccessCode._NO_CONTENT);
    }

    @Operation(
            summary = "해당 카테고리 조회",
            description =
                "<p> 카테고리 이름에 맞는 카테고리를 조회한다. </p>" +
                "<p> 카테고리 내부의 패턴도 함께 출력된다. </p>"
    )
    @GetMapping("/{category}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryPatternByCategory(@PathVariable String category) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                categoryPatternService.getCategoryPatternByCategory(category)
        );
    }

    @Operation(
            summary = "카테고리 전체 조회",
            description =
                "<p> 모든 카테고리를 조회한다. </p>" +
                "<p> 카테고리 내부의 패턴도 함께 출력된다. </p>"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                categoryPatternService.getCategories()
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "카테고리 수정",
            description =
                "<p> 카테고리의 이름과 색깔을 수정한다. </p>" +
                "<p> 색깔은 #000000 ~ #FFFFFF 로 입력한다. </p>" +
                "<p> 필요한 부분만 입력하고, 나머지는 비워둬도 된다. </p>"
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{category}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable String category,
            @RequestBody @Valid UpdateCategoryRequestDto updateCategoryRequestDto
    ) {
        categoryPatternService.updateCategory(category, updateCategoryRequestDto);
        return CustomResponseEntity.of(SuccessCode._OK);
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "카테고리 추가",
            description =
                "<p> 카테고리를 추가한다. </p>"
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addCategory(@RequestBody @Valid CategoryRequestDto categoryRequestDto) {
        categoryPatternService.addCategory(categoryRequestDto);
        return CustomResponseEntity.of(SuccessCode._OK);
    }
}

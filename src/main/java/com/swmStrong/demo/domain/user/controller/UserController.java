package com.swmStrong.demo.domain.user.controller;


import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.domain.user.service.UserService;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "유저 생성",
            description =
                    "<p> 새로운 유저를 생성한다.</p>"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<TokenResponseDto>> createGuestUser(HttpServletRequest request, @RequestBody @Valid UserRequestDto userRequestDto) {
        return CustomResponseEntity.of(
                SuccessCode._CREATED,
                userService.signupGuest(request, userRequestDto)
        );
    }

    @Operation(
            summary = "닉네임의 중복 여부를 확인한다.",
            description =
                    "<p> 중복일 경우 true, 중복이 아닐 경우 false를 반환한다. </p>"
    )
    @GetMapping("/nickname/check")
    public ResponseEntity<ApiResponse<Boolean>> isNicknameDuplicated(@Valid NicknameRequestDto nicknameRequestDto) {
        userService.validateNickname(nicknameRequestDto.nickname());
        return CustomResponseEntity.of(
                SuccessCode._OK,
                false
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 닉네임 변경",
            description =
                "<p> 유저 닉네임을 변경한다. </p>" +
                "<p> 유저의 토큰 인증이 필요하기 때문에 최초 소셜 로그인 단계에서도 토큰을 주입해두어야 한다. </p>" +
                "<p> 그리고 여기서 내려주는 정보도 guest를 생성했을 때와 같은 정보이기에 닉네임 바뀐걸 확인하고 덮어써도 된다. </p>"
    )
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateNickname(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestBody @Valid NicknameRequestDto nicknameRequestDto
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                userService.updateUserNickname(securityPrincipal.userId(), nicknameRequestDto)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "내 정보",
            description =
                "<p> 내 정보를 반환한다. </p>"
    )
    @GetMapping("/my-info")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(@AuthenticationPrincipal SecurityPrincipal securityPrincipal) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                userService.getInfoById(securityPrincipal.userId())
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "회원 정보 찾기",
            description =
                "<p> 입력한 userId나 nickname 에 해당하는 회원 정보를 반환한다. </p>" +
                "<p> 반드시 둘 중 하나만 입력해야한다. </p>"
    )
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserInfo(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String nickname
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                userService.getInfoByIdOrNickname(userId, nickname)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "회원 탈퇴",
            description =
                "<p> 회원 탈퇴를 한다. </p>" +
                "<p> soft delete는 아직 구현하지 않아 데이터(회원관련만)가 영구적으로 사라진다. </p>"
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal SecurityPrincipal securityPrincipal) {
        userService.deleteUserById(securityPrincipal.userId());
        return CustomResponseEntity.of(
                SuccessCode._NO_CONTENT
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "회원 탈퇴(관리자)",
            description =
                    "<p> 회원 탈퇴를 한다. (관리자용)</p>" +
                    "<p> soft delete는 아직 구현하지 않아 데이터(회원관련만)가 영구적으로 사라진다. </p>"
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        userService.deleteUserById(userId);
        return CustomResponseEntity.of(
                SuccessCode._NO_CONTENT
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "프로필 이미지 업로드",
            description = "<p>사용자의 프로필 이미지를 S3에 업로드하고 URL을 반환한다.</p>" +
                         "<p>지원하는 파일 형식: JPEG, JPG, PNG, GIF, WEBP</p>" +
                         "<p>최대 파일 크기: 5MB</p>"
    )
    @PostMapping("/profile-image")
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = userService.uploadProfileImage(securityPrincipal.userId(), file);
        return CustomResponseEntity.of(
                SuccessCode._CREATED,
                imageUrl
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "프로필 이미지 삭제",
            description = "<p>사용자의 프로필 이미지를 S3에서 삭제한다.</p>"
    )
    @DeleteMapping("/profile-image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal
    ) {
        userService.deleteProfileImage(securityPrincipal.userId());
        return CustomResponseEntity.of(
                SuccessCode._NO_CONTENT
        );
    }
}

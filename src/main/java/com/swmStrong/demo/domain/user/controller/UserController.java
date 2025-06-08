package com.swmStrong.demo.domain.user.controller;


import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.user.dto.NicknameRequestDto;
import com.swmStrong.demo.domain.user.dto.UnregisteredRequestDto;
import com.swmStrong.demo.domain.user.dto.UserRequestDto;
import com.swmStrong.demo.domain.user.dto.UserResponseDto;
import com.swmStrong.demo.domain.user.service.UserService;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "비회원")
@RestController
@RequiredArgsConstructor
@RequestMapping("/guest-users")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "유저 생성",
            description =
                    "<p> 새로운 유저를 생성한다.</p>"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createGuestUser(@RequestBody @Valid UserRequestDto userRequestDto) {
        return CustomResponseEntity.of(
                SuccessCode._CREATED,
                userService.registerGuestNickname(userRequestDto)
        );
    }

    @Operation(
            summary = "닉네임의 중복 여부를 확인한다.",
            description =
                    "<p> 중복일 경우 true, 중복이 아닐 경우 false를 반환한다. </p>"
    )
    @GetMapping("/is-nickname-duplicated")
    public ResponseEntity<ApiResponse<Boolean>> isNicknameDuplicated(@Valid NicknameRequestDto nicknameRequestDto) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                userService.isNicknameDuplicated(nicknameRequestDto.nickname())
        );
    }

    @Operation(
            summary = "비회원용 토큰 발급",
            description =
                "<p> 비회원용 토큰 발급 수단이다. </p>" +
                "<p> 등록일시같은 추가적인 유저 구분 수단을 반드시 보관하고 있고, 토큰 발급 시 넣어야 한다.</p>"
    )
    @PostMapping("/get-token")
    public ResponseEntity<ApiResponse<TokenResponseDto>> getToken(
            HttpServletRequest request,
            @RequestBody UnregisteredRequestDto unregisteredRequestDto
    ) {
        return CustomResponseEntity.of(
                SuccessCode._CREATED,
                userService.getToken(request, unregisteredRequestDto)
        );
    }

    @Operation(
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
}

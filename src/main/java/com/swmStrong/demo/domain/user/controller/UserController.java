package com.swmStrong.demo.domain.user.controller;


import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.domain.user.dto.UserRequestDto;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping()
    public ResponseEntity<ApiResponse<Void>> createGuestUser(@RequestBody UserRequestDto userRequestDto) {
        userService.registerGuestNickname(userRequestDto.deviceId(), userRequestDto.nickname());
        return ResponseEntity
                .status(SuccessCode._CREATED.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._CREATED, null));
    }

    @Operation(
            summary = "닉네임의 중복 여부를 확인한다.",
            description =
                    "<p> 중복일 경우 true, 중복이 아닐 경우 false를 반환한다. </p>"
    )
    @GetMapping("/is-nickname-duplicated")
    public ResponseEntity<ApiResponse<Boolean>> isGuestNicknameRegistered(@RequestParam(required = true) String nickname) {
        Boolean result = userService.isGuestNicknameDuplicated(nickname);
        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, result));
    }
}

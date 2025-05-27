package com.swmStrong.demo.domain.user.controller;


import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.domain.user.dto.UserRequestDto;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guest-users")

public class UserController {
    private final UserService userService;


    @PostMapping()
    public ResponseEntity<ApiResponse<Void>> createGuestUser(@RequestBody UserRequestDto userRequestDto) {
        userService.registerGuestNickname(userRequestDto.userId(), userRequestDto.nickname());
        return ResponseEntity
                .status(SuccessCode._CREATED.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._CREATED, null));
    }

    @GetMapping("/is-device-id-duplicated")
    public ResponseEntity<ApiResponse<Boolean>> isGuestUserRegistered(@RequestParam(required = true) String deviceId) {
        Boolean result = userService.isGuestRegistered(deviceId);
        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, result));
    }

    @GetMapping("/is-nickname-duplicated")
    public ResponseEntity<ApiResponse<Boolean>> isGuestNicknameRegistered(@RequestParam(required = true) String nickname) {
        Boolean result = userService.isGuestNicknameDuplicated(nickname);
        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, result));
    }

}

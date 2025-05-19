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
        userService.registerGuestNickname(userRequestDto.deviceId(), userRequestDto.nickname());
        return ResponseEntity
                .status(SuccessCode._CREATED.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._CREATED, null));
    }

    @GetMapping("/{userId}/isRegistered")
    public ResponseEntity<ApiResponse<Boolean>> isGuestUserRegistered(@PathVariable String userId) {
        Boolean result = userService.isGuestRegistered(userId);
        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, result));
    }

    @GetMapping("/{nickname}/isDuplicated")
    public ResponseEntity<ApiResponse<Boolean>> isGuestNicknameRegistered(@PathVariable String nickname) {
        Boolean result = userService.isGuestNicknameDuplicated(nickname);
        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, result));
    }

}

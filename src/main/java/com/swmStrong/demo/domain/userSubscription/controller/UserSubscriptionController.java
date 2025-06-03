package com.swmStrong.demo.domain.userSubscription.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.userSubscription.dto.ExistingUserSubscriptionReq;
import com.swmStrong.demo.domain.userSubscription.dto.NewUserSubscriptionReq;
import com.swmStrong.demo.domain.userSubscription.dto.UserSubscriptionRes;
import com.swmStrong.demo.domain.userSubscription.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor // Lombok
@RequestMapping("/users/subscriptions")
@Tag(name = "유저 구독")
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "새로 등록한 빌링키를 이용하여 바로 결제를 진행한다.",
            description =
                    "<p> 포트원 SDK를 이용해서 빌링키 발급을 한 후에 </p>" +
                            "<p> 구독한 플랜 정보와 빌링 키를 전달한다. </p>"
    )
    @PostMapping("/new")
    ResponseEntity<ApiResponse<Void>> createUserSubscriptionWithNewBillingKey(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestBody NewUserSubscriptionReq req) {
        userSubscriptionService.createUserSubscriptionWithBillingKey(
                securityPrincipal.userId(),
                req.subscriptionPlanId(),
                req.billingKey());

        return CustomResponseEntity.of(SuccessCode._OK);
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "기존의 결제 수단을 이용해서 바로 결제를 진행한다.",
            description =
                    "<p> 기존의 결제수단과 구독할 플랜의 아이디를 전달한다 </p>"
    )
    @PostMapping("/existing")
    ResponseEntity<ApiResponse<Void>> createUserSubscription(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestBody ExistingUserSubscriptionReq req) {
        userSubscriptionService.createUserSubscriptionWithPaymentMethod(
                securityPrincipal.userId(),
                req.subscriptionPlanId(),
                req.userPaymentMethodId());
        return CustomResponseEntity.of(SuccessCode._OK);
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "현재 구독한 플랜을 결제 취소한다 ( 이미 결제한 경우 )",
            description =
                    "<p> 구독 중인 플랜 결제를 취소한다. </p>"
    )
    @PutMapping("/current")
    ResponseEntity<ApiResponse<Void>> cancelCurrentSubscription(String userSubscriptionId, String reason) {
        userSubscriptionService.cancelCurrentSubscription(userSubscriptionId, reason);

        return CustomResponseEntity.of(SuccessCode._OK);
    }


    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = " 현재 구독 중인 플랜을 조회한다. ",
            description =
                    "<p> 해당 유저가 현재 구독 중인 플랜을 조회한다. </p>"
    )
    @GetMapping("/current")
    ResponseEntity<ApiResponse<UserSubscriptionRes>> getMyCurrentSubscription(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal) {
        UserSubscriptionRes userSubscriptionRes =
                userSubscriptionService.getCurrentSubscription(securityPrincipal.userId());

        return CustomResponseEntity.of(SuccessCode._OK, userSubscriptionRes);
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = " 유저가 구독했던 모든 플랜을. ",
            description =
                    "<p> 해당 유저가 구독 했던 모든 플랜을 조회한다. </p>"
    )
    @GetMapping("")
    ResponseEntity<ApiResponse<List<UserSubscriptionRes>>> getMySubscriptions(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal) {
        List <UserSubscriptionRes> userSubscriptionResList =
                userSubscriptionService.getAllSubscriptions(securityPrincipal.userId());


        return CustomResponseEntity.of(SuccessCode._OK, userSubscriptionResList);
    }




}
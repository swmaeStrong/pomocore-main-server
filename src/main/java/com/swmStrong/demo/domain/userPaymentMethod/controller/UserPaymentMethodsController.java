package com.swmStrong.demo.domain.userPaymentMethod.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.service.UsageLogService;
import com.swmStrong.demo.domain.userPaymentMethod.dto.BillingKeyReq;
import com.swmStrong.demo.domain.userPaymentMethod.dto.UserPaymentMethodRes;
import com.swmStrong.demo.domain.userPaymentMethod.service.UserPaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@Tag(name = "유저 결제 수단")
@RestController
@RequestMapping("/users/payment-methods")
public class UserPaymentMethodsController {


    private final UserPaymentMethodService userPaymentMethodService;

    public UserPaymentMethodsController(UserPaymentMethodService userPaymentMethodService) {
        this.userPaymentMethodService = userPaymentMethodService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저가 갖고 있는 결제 수단 조회",
            description =
                    "<p> 유저의 전체 결제 수단을 조회한다. </p>"+
                    "<p> easyPay 결제수단의 경우 (ex, kakaopay) pg사 정보만 나온다.</p>" +
                    "<p> 타 결제수단의 경우 (ex, tossPayments) pg사, 카드사, 카드번호 정보가 같이 나온다.</p>"

    )
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<UserPaymentMethodRes>>> getMyPaymentMethods(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal
    ) {
        List<UserPaymentMethodRes> userPaymentMethodResList =
                userPaymentMethodService.getMyPaymentMethods(securityPrincipal.userId());

        return CustomResponseEntity.of(SuccessCode._OK, userPaymentMethodResList);
    }


    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저가 결제 수단 등록",
            description =
                    "<p> 유저의 빌링 키를 전달해서 결제 수단을 등록한다. </p>"
    )
    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> storeMyPaymentMethods(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestBody BillingKeyReq billingKeyReq
    ) {
        userPaymentMethodService.storeMyPaymentMethod(securityPrincipal.userId(), billingKeyReq.billilngKey());
        return CustomResponseEntity.of(SuccessCode._OK);
    }



}

package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.service.MemberService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 계정 관리(비밀번호 변경, 탈퇴) 전용 컨트롤러.
 */
@Tag(name = "회원 계정", description = "비밀번호 변경 및 회원 탈퇴 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberAccountController {

    private final MemberService memberService;

    @Operation(
            summary = "비밀번호 변경",
            description = "회원의 비밀번호를 변경합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 비밀번호 정책 위반"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 또는 기존 비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "비밀번호 실패 횟수 초과"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(@UserContext MemberDto memberDto,
                                            @Valid @RequestBody PasswordChangeRequest request)
            throws PasswordPolicyException, PasswordFailedExceededException {
        memberService.checkPasswordDoubly(request.getNewPassword(), request.getConfirmPassword());
        memberService.changePassword(memberDto.getMemberId(), request.getOriginPassword(), request.getNewPassword());
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "회원을 탈퇴하고 관련 데이터를 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMember(@UserContext MemberDto memberDto) {
        memberService.deleteByMemberId(memberDto.getMemberId());
        return ApiResponse.createSuccessWithNoContent();
    }


    @Data
    @AllArgsConstructor
    public static class PasswordChangeRequest {
        private String originPassword;
        private String newPassword;
        private String confirmPassword;
    }
} 
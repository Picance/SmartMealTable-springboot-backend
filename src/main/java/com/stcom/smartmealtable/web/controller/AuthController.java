package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.MemberService;
import com.stcom.smartmealtable.service.TermService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.service.dto.TermAgreementRequestDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증(로그인, 회원가입) 관련 API.
 */
@Tag(name = "인증", description = "회원가입, 로그인, 이메일 중복 확인, 약관 동의 관련 API")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final MemberService memberService;
    private final JwtTokenService jwtTokenService;
    private final TermService termService;

    @Operation(
            summary = "이메일 중복 확인",
            description = "회원가입 시 이메일 중복 여부를 확인합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용 가능한 이메일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "중복된 이메일 또는 잘못된 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/email/check")
    public ResponseEntity<ApiResponse<Void>> checkEmail(
            @Parameter(description = "중복 확인할 이메일 주소", example = "test@example.com")
            @Email @RequestParam String email) {
        memberService.validateDuplicatedEmail(email);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent());
    }

    @Operation(
            summary = "회원가입",
            description = "새로운 회원을 등록하고 JWT 토큰을 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = JwtTokenResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public ApiResponse<JwtTokenResponseDto> signUp(@Valid @RequestBody SignUpRequest request)
            throws PasswordPolicyException {
        memberService.validateDuplicatedEmail(request.getEmail());
        memberService.checkPasswordDoubly(request.getPassword(), request.getConfirmPassword());

        Member member = Member.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .rawPassword(request.getPassword())
                .build();

        memberService.saveMember(member);
        JwtTokenResponseDto tokenDto = jwtTokenService.createTokenDto(member.getId(), null);
        tokenDto.setNewUser(true);
        return ApiResponse.createSuccess(tokenDto);
    }

    @Operation(
            summary = "약관 동의",
            description = "회원가입 후 필수 및 선택 약관에 동의합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "약관 동의 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/signup/terms")
    public ApiResponse<Void> agreeTerms(@UserContext MemberDto memberDto,
                                        @RequestBody List<TermAgreementRequest> agreements) {
        termService.agreeTerms(
                memberDto.getMemberId(),
                agreements.stream()
                        .map(dto -> new TermAgreementRequestDto(dto.getTermId(), dto.getIsAgreed()))
                        .toList()
        );
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "회원가입 취소",
            description = "회원가입을 취소하고 등록된 회원 정보를 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 취소 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/signup")
    public ApiResponse<Void> cancelSignUp(@UserContext MemberDto memberDto) {
        memberService.deleteByMemberId(memberDto.getMemberId());
        return ApiResponse.createSuccessWithNoContent();
    }

    @Data
    @AllArgsConstructor
    public static class SignUpRequest {
        @Email(message = "유효한 이메일 형식이 아닙니다")
        @NotEmpty(message = "이메일은 비어있을 수 없습니다")
        private String email;
        @NotEmpty(message = "비밀번호는 비어있을 수 없습니다")
        private String password;
        @NotEmpty(message = "비밀번호 확인은 비어있을 수 없습니다")
        private String confirmPassword;
        @NotEmpty(message = "이름은 비어있을 수 없습니다")
        private String fullName;
    }

    @Data
    @AllArgsConstructor
    public static class TermAgreementRequest {
        private Long termId;
        private Boolean isAgreed;
    }
} 
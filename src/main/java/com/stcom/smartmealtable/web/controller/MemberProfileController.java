package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.service.MemberProfileService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 프로필", description = "회원 프로필 조회, 생성, 수정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/profiles")
public class MemberProfileController {

    private final MemberProfileService memberProfileService;

    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 회원의 프로필 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberProfilePageResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/me")
    public ApiResponse<MemberProfilePageResponse> getMemberProfilePageInfo(@UserContext MemberDto memberDto) {
        MemberProfile profile = memberProfileService.getProfileFetch(memberDto.getProfileId());
        return ApiResponse.createSuccess(new MemberProfilePageResponse(profile, memberDto));
    }

    @Operation(
            summary = "회원 프로필 생성",
            description = "새로운 회원 프로필을 생성합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 프로필이 존재함"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ApiResponse<Void> createMemberProfile(@UserContext MemberDto memberDto,
                                                 @Validated @RequestBody MemberProfileRequest request) {
        memberProfileService.createProfile(request.getNickName(), memberDto.getMemberId(), request.getMemberType(),
                request.getGroupId());
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "내 프로필 수정",
            description = "현재 로그인한 회원의 프로필 정보를 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/me")
    public ApiResponse<Void> changeMemberProfile(@UserContext MemberDto memberDto,
                                                 @Validated @RequestBody MemberProfileRequest request) {
        memberProfileService.changeProfile(memberDto.getProfileId(), request.getNickName(), request.getMemberType(),
                request.getGroupId());
        return ApiResponse.createSuccessWithNoContent();
    }

    @AllArgsConstructor
    @Data
    static class MemberProfilePageResponse {
        private String nickName;
        private String email;
        private MemberType memberType;
        private String groupName;
        private String primaryAddress;

        public MemberProfilePageResponse(MemberProfile profile, MemberDto memberDto) {
            this.nickName = profile.getNickName();
            this.email = memberDto.getEmail();
            this.primaryAddress = profile.findPrimaryAddress().getAddress().getRoadAddress()
                    + profile.findPrimaryAddress().getAddress().getDetailAddress();
            this.memberType = profile.getType();
            this.groupName = profile.getGroup().getName();
        }
    }

    @AllArgsConstructor
    @Data
    static class MemberProfileRequest {
        @NotEmpty(message = "닉네임은 비어있을 수 없습니다")
        private String nickName;
        private Long groupId;
        private MemberType memberType;
    }
}

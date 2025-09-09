package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import com.stcom.smartmealtable.domain.food.PreferenceType;
import com.stcom.smartmealtable.service.MemberCategoryPreferenceService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "회원 선호도", description = "회원의 음식 카테고리 선호도 설정 및 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/preferences")
public class MemberPreferenceController {

    private final MemberCategoryPreferenceService memberCategoryPreferenceService;

    @Operation(
            summary = "내 선호도 조회",
            description = "회원의 음식 카테고리 선호도를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "선호도 조회 성공",
                    content = @Content(schema = @Schema(implementation = PreferencesResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ApiResponse<PreferencesResponse> getCategoryPreferences(@UserContext MemberDto memberDto) {
        List<MemberCategoryPreference> preferences =
                memberCategoryPreferenceService.getPreferences(memberDto.getProfileId());

        List<CategoryPreferenceDto> liked = preferences.stream()
                .filter(p -> p.getType() == PreferenceType.LIKE)
                .map(p -> new CategoryPreferenceDto(
                        p.getCategory().getId(),
                        p.getCategory().getName(),
                        p.getPriority()))
                .toList();

        List<CategoryPreferenceDto> disliked = preferences.stream()
                .filter(p -> p.getType() == PreferenceType.DISLIKE)
                .map(p -> new CategoryPreferenceDto(
                        p.getCategory().getId(),
                        p.getCategory().getName(),
                        p.getPriority()))
                .toList();

        return ApiResponse.createSuccess(new PreferencesResponse(liked, disliked));
    }

    @Operation(
            summary = "선호도 저장",
            description = "회원의 음식 카테고리 선호도를 저장합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "선호도 저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ApiResponse<Void> saveCategoryPreferences(@UserContext MemberDto memberDto,
                                                     @RequestBody PreferencesRequest request) {
        memberCategoryPreferenceService.savePreferences(
                memberDto.getProfileId(),
                request.getLiked(),
                request.getDisliked());
        return ApiResponse.createSuccessWithNoContent();
    }

    @AllArgsConstructor
    @Data
    static class PreferencesRequest {
        private List<Long> liked;
        private List<Long> disliked;
    }

    @AllArgsConstructor
    @Data
    static class PreferencesResponse {
        private List<CategoryPreferenceDto> liked;
        private List<CategoryPreferenceDto> disliked;
    }

    @AllArgsConstructor
    @Data
    static class CategoryPreferenceDto {
        private Long categoryId;
        private String categoryName;
        private Integer priority;
    }
} 
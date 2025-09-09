package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import com.stcom.smartmealtable.service.GroupService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import com.stcom.smartmealtable.web.dto.group.SchoolGroupCreateRequest;
import com.stcom.smartmealtable.web.dto.group.SchoolGroupUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 학교 그룹 전용 API.
 */
@Tag(name = "학교 그룹", description = "학교 그룹 등록, 수정, 삭제 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schools")
public class SchoolGroupController {

    private final GroupService groupService;

    @Operation(
            summary = "학교 그룹 등록",
            description = "새로운 학교 그룹을 등록합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "학교 그룹 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 학교 그룹"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping()
    public ApiResponse<Void> registerSchoolGroup(@RequestBody @Valid SchoolGroupCreateRequest request) {
        groupService.createSchoolGroup(new AddressRequest(request.getRoadAddress(), request.getDetailAddress()),
                request.getName(), request.getType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "학교 그룹 수정",
            description = "기존 학교 그룹 정보를 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "학교 그룹 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "학교 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{id}")
    public ApiResponse<Void> editSchoolGroup(
            @Parameter(description = "수정할 학교 그룹 ID", example = "1")
            @PathVariable("id") Long id,
            @RequestBody @Valid SchoolGroupUpdateRequest request) {
        groupService.changeSchoolGroup(id,
                new AddressRequest(request.getRoadAddress(), request.getDetailAddress()),
                request.getName(), request.getType());
        return ApiResponse.createSuccessWithNoContent();
    }
    
    @Operation(
            summary = "학교 그룹 삭제",
            description = "기존 학교 그룹을 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "학교 그룹 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "학교 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSchoolGroup(
            @Parameter(description = "삭제할 학교 그룹 ID", example = "1")
            @PathVariable("id") Long id) {
        groupService.deleteGroup(id);
        return ApiResponse.createSuccessWithNoContent();
    }
} 
package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.service.GroupService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import com.stcom.smartmealtable.web.dto.group.GroupDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "그룹", description = "그룹 검색 및 삭제 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    @Operation(
            summary = "그룹 검색",
            description = "키워드를 사용하여 그룹을 검색합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "그룹 검색 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "비어있는 키워드"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping()
    public ApiResponse<List<GroupDto>> searchGroup(
            @Parameter(description = "검색할 그룹 키워드", example = "서울대학교")
            @RequestParam String keyword) {
        if (keyword.isBlank()) {
            return ApiResponse.createError("키워드가 비어있습니다. 키워드를 입력해주세요");
        }
        List<Group> result = groupService.findGroupsByKeyword(keyword);
        return ApiResponse.createSuccess(result.stream()
                .map(GroupDto::new)
                .toList());
    }

    @Operation(
            summary = "그룹 삭제",
            description = "지정된 ID의 그룹을 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "그룹 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteGroup(
            @Parameter(description = "삭제할 그룹 ID", example = "1")
            @PathVariable("id") Long id) {
        groupService.deleteGroup(id);
        return ApiResponse.createSuccessWithNoContent();
    }
}

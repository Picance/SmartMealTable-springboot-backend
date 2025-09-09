package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.infrastructure.AddressApiService;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import com.stcom.smartmealtable.service.MemberProfileService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 주소", description = "회원의 주소 등록, 수정, 삭제 및 기본 주소 설정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/addresses")
public class MemberAddressController {

    private final MemberProfileService memberProfileService;
    private final AddressApiService addressApiService;

    @Operation(
            summary = "기본 주소 변경",
            description = "지정된 주소를 기본 주소로 설정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기본 주소 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{id}/primary")
    public ApiResponse<Void> changePrimaryAddress(@UserContext MemberDto memberDto,
                                                  @Parameter(description = "기본 주소로 설정할 주소 ID", example = "1")
                                                  @PathVariable("id") Long addressId) {
        memberProfileService.changeAddressToPrimary(memberDto.getProfileId(), addressId);
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "주소 등록",
            description = "새로운 주소를 등록합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ApiResponse<Void> registerAddress(@UserContext MemberDto memberDto,
                                             @Validated @RequestBody MemberAddressCURequest request) {
        Address address = addressApiService.createAddressFromRequest(request.toAddressApiRequest());
        memberProfileService.saveNewAddress(memberDto.getProfileId(), address, request.getAlias(),
                request.getAddressType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "주소 수정",
            description = "기존 주소 정보를 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{id}")
    public ApiResponse<Void> changeAddress(@UserContext MemberDto memberDto,
                                           @Parameter(description = "수정할 주소 ID", example = "1")
                                           @PathVariable("id") Long addressId,
                                           @Validated @RequestBody MemberAddressCURequest request) {
        Address address = addressApiService.createAddressFromRequest(request.toAddressApiRequest());
        memberProfileService.changeAddress(memberDto.getProfileId(), addressId, address, request.getAlias(),
                request.getAddressType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "주소 삭제",
            description = "기존 주소를 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주소 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@UserContext MemberDto memberDto,
                                           @Parameter(description = "삭제할 주소 ID", example = "1")
                                           @PathVariable("id") Long addressId) {
        memberProfileService.deleteAddress(memberDto.getProfileId(), addressId);
        return ApiResponse.createSuccessWithNoContent();
    }

    @AllArgsConstructor
    @Data
    static class MemberAddressCURequest {
        private String roadAddress;
        private AddressType addressType;
        private String alias;
        private String detailAddress;

        public AddressRequest toAddressApiRequest() {
            return new AddressRequest(roadAddress, detailAddress);
        }
    }
} 
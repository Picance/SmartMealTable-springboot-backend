package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.component.creditmessage.CreditMessageManager;
import com.stcom.smartmealtable.component.creditmessage.ExpenditureDto;
import com.stcom.smartmealtable.domain.Budget.Expenditure;
import com.stcom.smartmealtable.service.ExpenditureService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 지출 내역", description = "지출 내역 조회, 등록, 수정, 삭제 및 신용카드 메시지 파싱 관련 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/members/me/expenditures")
public class MemberExpenditureController {

    private final ExpenditureService expenditureService;
    private final CreditMessageManager creditMessageManager;

    @Operation(
            summary = "신용카드 메시지 파싱",
            description = "신용카드 사용 알림 메시지를 파싱하여 지출 정보를 추출합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메시지 파싱 성공",
                    content = @Content(schema = @Schema(implementation = ExpenditureDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 메시지 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/messages/parse")
    public ApiResponse<ExpenditureDto> parseCreditMessage(@RequestBody ParseRequest request) {
        return ApiResponse.createSuccess(creditMessageManager.parseMessage(request.getMessage()));
    }

    @Operation(
            summary = "지출 내역 조회",
            description = "회원의 지출 내역을 페이지별로 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "지출 내역 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ApiResponse<Slice<ExpenditureResponse>> getExpenditures(@UserContext MemberDto memberDto,
                                                                   @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                                   @Parameter(description = "페이지 크기", example = "10")
                                                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        Slice<Expenditure> slice = expenditureService.getExpenditures(memberDto.getProfileId(), page, size);
        Slice<ExpenditureResponse> responseSlice = slice.map(ExpenditureResponse::of);
        return ApiResponse.createSuccess(responseSlice);
    }

    @Operation(
            summary = "지출 내역 등록",
            description = "새로운 지출 내역을 등록합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "지출 내역 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ApiResponse<Void> registerExpenditure(@UserContext MemberDto memberDto,
                                                 @RequestBody @Validated ExpenditureRequest request) {
        expenditureService.registerExpenditure(
                memberDto.getProfileId(),
                request.getSpentDate(),
                request.getAmount(),
                request.getTradeName()
        );
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "지출 내역 수정",
            description = "기존 지출 내역을 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "지출 내역 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "지출 내역을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{id}")
    public ApiResponse<Void> editExpenditure(@UserContext MemberDto memberDto, 
                                             @Parameter(description = "수정할 지출 내역 ID", example = "1")
                                             @PathVariable("id") Long expenditureId,
                                             @RequestBody @Validated ExpenditureRequest request) {
        expenditureService.editExpenditure(
                memberDto.getProfileId(),
                expenditureId,
                request.getSpentDate(),
                request.getAmount(),
                request.getTradeName()
        );
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "지출 내역 삭제",
            description = "기존 지출 내역을 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "지출 내역 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "지출 내역을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteExpenditure(@UserContext MemberDto memberDto,
                                               @Parameter(description = "삭제할 지출 내역 ID", example = "1")
                                               @PathVariable("id") Long expenditureId) {
        expenditureService.deleteExpenditure(memberDto.getProfileId(), expenditureId);
        return ApiResponse.createSuccessWithNoContent();
    }

    @Data
    static class ParseRequest {

        @NotEmpty
        private String message;

    }

    @Data
    static class ExpenditureRequest {

        @DateTimeFormat(iso = ISO.DATE_TIME)
        @NotNull
        private LocalDateTime spentDate;

        @NotNull
        @Positive
        private Long amount;

        @NotEmpty
        private String tradeName;
    }

    @Data
    @AllArgsConstructor
    static class ExpenditureResponse {

        private Long id;
        private LocalDateTime spentDate;
        private Long amount;
        private String tradeName;

        public static ExpenditureResponse of(Expenditure expenditure) {
            return new ExpenditureResponse(expenditure.getId(), expenditure.getSpentDate(), expenditure.getAmount(),
                    expenditure.getTradeName());
        }
    }
}

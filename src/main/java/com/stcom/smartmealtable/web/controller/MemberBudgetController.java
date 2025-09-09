package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.Budget.Budget;
import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.service.BudgetService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import com.stcom.smartmealtable.web.validation.YearMonthFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 예산", description = "일별/월별 예산 조회, 등록, 수정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/budgets")
public class MemberBudgetController {

    private final BudgetService budgetService;

    @Operation(
            summary = "일별 예산 조회",
            description = "특정 날짜의 일별 예산 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일별 예산 조회 성공",
                    content = @Content(schema = @Schema(implementation = DailyBudgetResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/daily/{date}")
    public ApiResponse<DailyBudgetResponse> dailyBudgetByDate(@UserContext MemberDto memberDto,
                                                              @Parameter(description = "조회할 날짜", example = "2024-01-15")
                                                              @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        DailyBudget dailyBudget = budgetService.getDailyBudgetBy(memberDto.getProfileId(), date);
        return ApiResponse.createSuccess(DailyBudgetResponse.of(dailyBudget));
    }

    @Operation(
            summary = "기본 일별 예산 등록",
            description = "특정 날짜에 기본 일별 예산 한도를 등록합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일별 예산 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/daily/{date}/default")
    public ApiResponse<Void> registerDefaultDailyBudget(@UserContext MemberDto memberDto,
                                                        @Parameter(description = "등록할 날짜", example = "2024-01-15")
                                                        @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date,
                                                        @Parameter(description = "일별 예산 한도 (원)", example = "30000")
                                                        @RequestParam("limit") Long limit) {
        budgetService.registerDefaultDailyBudgetBy(memberDto.getProfileId(), limit, date);
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "일별 예산 수정",
            description = "특정 날짜의 일별 예산 한도를 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일별 예산 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 날짜의 예산을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/daily/{date}")
    public ApiResponse<Void> editDailyBudget(@UserContext MemberDto memberDto,
                                             @Parameter(description = "수정할 날짜", example = "2024-01-15")
                                             @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) String date,
                                             @Parameter(description = "수정할 일별 예산 한도 (원)", example = "35000")
                                             @RequestParam("limit") Long limit) {
        budgetService.editDailyBudgetCustom(memberDto.getProfileId(), LocalDate.parse(date), limit);
        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "주간 일별 예산 조회",
            description = "특정 날짜가 속한 주의 일별 예산 데이터를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주간 일별 예산 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/daily/{date}/week")
    public ApiResponse<List<DailyBudgetResponse>> dailyBudgetWeekByDate(@UserContext MemberDto memberDto,
                                                                        @Parameter(description = "기준 날짜", example = "2024-01-15")
                                                                        @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        List<DailyBudget> dailyBudgets = budgetService.getDailyBudgetsByWeek(memberDto.getProfileId(),
                date);

        List<DailyBudgetResponse> responses = dailyBudgets.stream()
                .map(DailyBudgetResponse::of)
                .toList();

        return ApiResponse.createSuccess(responses);
    }

    @Operation(
            summary = "월별 예산 목록 조회",
            description = "특정 날짜가 속한 달을 포함하여 이전 6개월의 월별 예산 데이터를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 예산 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/montly/{date}")
    public ApiResponse<List<MonthlyBudgetResponse>> monthlyBudgetsByDate(@UserContext MemberDto memberDto,
                                                                         @Parameter(description = "기준 날짜", example = "2024-01-15")
                                                                         @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        List<MonthlyBudget> monthlyBudgets = budgetService.getMonthlyBudgetsBy(memberDto.getProfileId(),
                date, 6);

        List<MonthlyBudgetResponse> responses = monthlyBudgets.stream()
                .map(MonthlyBudgetResponse::of)
                .toList();

        return ApiResponse.createSuccess(responses);
    }

    @Operation(
            summary = "월별 예산 조회",
            description = "특정 연월의 월별 예산 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 예산 조회 성공",
                    content = @Content(schema = @Schema(implementation = MonthlyBudgetResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/monthly/{yearMonth}")
    public ApiResponse<MonthlyBudgetResponse> monthlyBudgetByDate(@UserContext MemberDto memberDto,
                                                                  @Parameter(description = "조회할 연월", example = "2024-01")
                                                                  @PathVariable("yearMonth") @YearMonthFormat YearMonth yearMonth) {
        MonthlyBudget monthlyBudget = budgetService.getMonthlyBudgetBy(memberDto.getProfileId(),
                yearMonth);

        return ApiResponse.createSuccess(MonthlyBudgetResponse.of(monthlyBudget));
    }

    @Operation(
            summary = "기본 월별 예산 등록",
            description = "특정 연월에 기본 월별 예산 한도를 등록합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 예산 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/monthly/{yearMonth}/default")
    public ApiResponse<Void> registerDefaultMonthlyBudget(@UserContext MemberDto memberDto,
                                                          @Parameter(description = "등록할 연월", example = "2024-01")
                                                          @PathVariable("yearMonth") @YearMonthFormat YearMonth yearMonth,
                                                          @Parameter(description = "월별 예산 한도 (원)", example = "500000")
                                                          @RequestParam("limit") Long limit) {
        budgetService.registerDefaultMonthlyBudgetBy(memberDto.getProfileId(),
                limit, yearMonth);

        return ApiResponse.createSuccessWithNoContent();
    }

    @Operation(
            summary = "월별 예산 수정",
            description = "특정 연월의 월별 예산 한도를 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 예산 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 연월의 예산을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/monthly/{yearMonth}")
    public ApiResponse<Void> editMonthlyBudget(@UserContext MemberDto memberDto,
                                               @Parameter(description = "수정할 연월", example = "2024-01")
                                               @PathVariable("yearMonth") @YearMonthFormat YearMonth yearMonth,
                                               @Parameter(description = "수정할 월별 예산 한도 (원)", example = "600000")
                                               @RequestParam("limit") Long limit) {
        budgetService.editMonthlyBudgetCustom(memberDto.getProfileId(),
                yearMonth, limit);

        return ApiResponse.createSuccessWithNoContent();
    }

    @AllArgsConstructor
    @Data
    static class DailyBudgetResponse {
        private Long dailySpentAmount;
        private Long dailyLimitAmount;
        private Long dailyAvailableAmount;

        public static DailyBudgetResponse of(Budget dailyBudget) {
            return new DailyBudgetResponse(
                    dailyBudget.getSpendAmount().longValue(),
                    dailyBudget.getLimit().longValue(),
                    dailyBudget.getAvailableAmount().longValue()
            );
        }
    }

    @AllArgsConstructor
    @Data
    static class MonthlyBudgetResponse {
        private Long monthlySpentAmount;
        private Long monthlyLimitAmount;
        private Long monthlyAvailableAmount;

        public static MonthlyBudgetResponse of(Budget monthlyBudget) {
            return new MonthlyBudgetResponse(
                    monthlyBudget.getSpendAmount().longValue(),
                    monthlyBudget.getLimit().longValue(),
                    monthlyBudget.getAvailableAmount().longValue()
            );
        }
    }
} 
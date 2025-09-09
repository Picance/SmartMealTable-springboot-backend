package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.term.Term;
import com.stcom.smartmealtable.service.TermService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "약관", description = "이용약관 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/terms")
public class TermController {

    private final TermService termService;

    @Operation(
            summary = "약관 목록 조회",
            description = "모든 이용약관 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "약관 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = TermResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping()
    public ApiResponse<List<TermResponse>> getTerms() {
        List<Term> result = termService.findAll();
        return ApiResponse.createSuccess(result.stream()
                .map(TermResponse::new)
                .toList());
    }

    @Data
    @AllArgsConstructor
    static class TermResponse {
        private Long termId;
        private String title;
        private String content;
        private boolean isRequired;

        public TermResponse(Term term) {
            this.termId = term.getId();
            this.title = term.getTitle();
            this.content = term.getContent();
            this.isRequired = term.getIsRequired();
        }

    }

}

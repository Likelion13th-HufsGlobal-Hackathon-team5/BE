package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.KeywordScrollResDto;
import com.hackathon_5.Yogiyong_In.DTO.KeywordUpdateReqDto;
import com.hackathon_5.Yogiyong_In.DTO.KeywordGetItemDto;
import com.hackathon_5.Yogiyong_In.service.KeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Keyword", description = "키워드 조회/선택 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class KeywordController {

    private final KeywordService keywordService;

    @Operation(
            summary = "키워드 목록(스크롤)",
            description = """
                cursor(마지막 keywordId) 이후 데이터를 size만큼 반환.
                includeSelected=true 이고 (로그인 or X-Device-Id 제공) 상태면 selected 필드를 포함합니다.
                게스트 사용자는 X-Device-Id 헤더를 보내 주세요.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
    })
    @GetMapping("/keywords")
    public ResponseEntity<KeywordScrollResDto> getKeywords(
            @Parameter(description = "이 커서(마지막 keywordId) 이후부터 조회")
            @RequestParam(required = false) Integer cursor,
            @Parameter(description = "가져올 개수(기본 30, 최대 100)")
            @RequestParam(defaultValue = "30") Integer size,
            @Parameter(description = "선택 여부 표시 포함 (게스트는 X-Device-Id 필요)")
            @RequestParam(defaultValue = "false") boolean includeSelected,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId
    ) {
        int s = (size == null || size <= 0) ? 30 : Math.min(size, 100);
        return ResponseEntity.ok(
                // 새 오버로드: 게스트도 포함 처리
                keywordService.getKeywordsScroll(cursor, s, includeSelected, authHeader, deviceId)
        );
    }

    @Operation(
            summary = "내 선택 키워드 전체 교체",
            description = """
                전달한 keywordIds만 선택 상태로 저장합니다(전체 교체).
                로그인은 Authorization 헤더를, 비로그인은 X-Device-Id 헤더(고정 UUID)를 보내세요.
                expand=names 로 보내면 ID+이름을 함께 반환합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청/헤더 누락", content = @Content)
    })
    @Parameters({
            @Parameter(name = "expand", description = "names 사용 시 ID+이름 반환 (ex. ?expand=names)", schema = @Schema(allowableValues = {"names"}))
    })
    @PutMapping("/me/selected-keywords")
    public ResponseEntity<?> replaceMyKeywords(
            @RequestBody KeywordUpdateReqDto body,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestParam(name = "expand", required = false) String expand
    ) {
        // 서비스에서 로그인/게스트 판별 + 저장
        keywordService.replaceUserKeywords(authHeader, deviceId, body.getKeywordIds());

        if ("names".equalsIgnoreCase(expand)) {
            List<KeywordGetItemDto> items = keywordService.getSelectedKeywordsWithNames(authHeader, deviceId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of("selectedKeywords", items),
                    "error", null
            ));
        } else {
            List<Integer> ids = (body.getKeywordIds() == null) ? List.of() : body.getKeywordIds();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of("selectedKeywordIds", ids),
                    "error", null
            ));
        }
    }

    // (선택) Admin/Legacy API
    @Deprecated
    @Operation(
            summary = "(Admin/Legacy) 특정 유저의 선택 키워드 전체 교체",
            description = "일반 사용자는 자신과 일치하는 userId만 허용됩니다. 신규로는 /api/me/selected-keywords 사용을 권장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "저장 성공(본문 없음)"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 키워드", content = @Content)
    })
    @PutMapping("/users/{userId}/keywords")
    public ResponseEntity<Void> updateUserKeywords(
            @PathVariable String userId,
            @RequestBody KeywordUpdateReqDto body
    ) {
        // 서비스 최신 시그니처(2-파라미터)로 호출
        keywordService.replaceUserKeywords(userId, body.getKeywordIds());
        return ResponseEntity.noContent().build();
    }
}

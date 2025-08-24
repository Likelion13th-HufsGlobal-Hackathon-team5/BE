package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.dto.Keyword.KeywordScrollResDto;
import com.hackathon_5.Yogiyong_In.dto.Keyword.KeywordUpdateReqDto;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Tag(name = "Keyword", description = "키워드 조회/선택 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class KeywordController {

    private final KeywordService keywordService;

    @Operation(
            summary = "키워드 목록(스크롤)",
            description = """
            cursor(마지막 keywordId) 이후 데이터를 size만큼 반환합니다.
            ✅ 로그인 없이도 조회 가능.
            단, includeSelected=true는 로그인한 경우에만 적용됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/keywords")
    public ResponseEntity<KeywordScrollResDto> getKeywords(
            @Parameter(description = "이 커서(마지막 keywordId) 이후부터 조회")
            @RequestParam(required = false) Integer cursor,
            @Parameter(description = "가져올 개수(기본 30, 최대 100)")
            @RequestParam(defaultValue = "30") Integer size,
            @Parameter(description = "선택 여부 포함(로그인 시에만 적용)")
            @RequestParam(defaultValue = "false") boolean includeSelected
    ) {
        // SecurityContext에서 현재 사용자 ID (없으면 null)
        String userId = com.hackathon_5.Yogiyong_In.util.AuthUtils.currentUserIdOrNull();

        // ✅ 가독성 좋은 size 계산
        int s = (size == null || size <= 0) ? 30 : Math.min(size, 100);
        boolean effectiveIncludeSelected = includeSelected && userId != null;

        return ResponseEntity.ok(
                keywordService.getKeywordsScroll(cursor, s, effectiveIncludeSelected, userId)
        );
    }

    @Operation(
            summary = "내 선택 키워드 전체 교체",
            description = """
            전달한 keywordIds만 선택 상태로 저장합니다(전체 교체).
            ✅ 로그인 사용자 전용입니다.
            expand=names 를 사용하면 ID+이름을 함께 반환합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 키워드", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @Parameters({
            @Parameter(
                    name = "expand",
                    description = "names 사용 시 ID+이름 반환 (예: ?expand=names)",
                    schema = @Schema(allowableValues = {"names"})
            )
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/selected-keywords")
    public ResponseEntity<?> replaceMyKeywords(
            @RequestBody KeywordUpdateReqDto body,
            @RequestParam(name = "expand", required = false) String expand
    ) {
        String userId = com.hackathon_5.Yogiyong_In.util.AuthUtils.currentUserIdOrThrow();

        List<Integer> keywordIds = (body.getKeywordIds() == null) ? List.of() : body.getKeywordIds();
        keywordService.replaceUserKeywords(userId, keywordIds);

        // ✅ null 허용을 위해 HashMap 사용
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("error", null);

        if ("names".equalsIgnoreCase(expand)) {
            var items = keywordService.getSelectedKeywordsWithNames(userId);
            if (items == null) items = List.of(); // null-safe
            responseBody.put("data", Map.of("userId", userId, "selectedKeywords", items));
        } else {
            responseBody.put("data", Map.of("userId", userId, "selectedKeywordIds", keywordIds));
        }

        return ResponseEntity.ok(responseBody);
    }

    @Deprecated
    @Operation(
            summary = "(Admin/Legacy) 특정 유저의 선택 키워드 전체 교체",
            description = "일반 사용자는 자신과 일치하는 userId만 허용됩니다. 신규로는 /api/me/selected-keywords 사용 권장."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "저장 성공(본문 없음)"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 키워드", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "접근 거부", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/users/{userId}/keywords")
    public ResponseEntity<Void> updateUserKeywords(
            @PathVariable String userId,
            @RequestBody KeywordUpdateReqDto body
    ) {
        String me = com.hackathon_5.Yogiyong_In.util.AuthUtils.currentUserIdOrThrow();
        if (!me.equals(userId)) {
            // 다른 사용자 리소스 수정 → 403이 의미상 더 적절
            throw new ResponseStatusException(FORBIDDEN, "다른 사용자의 키워드는 수정할 수 없습니다.");
        }
        // ✅ null-safe
        List<Integer> ids = (body.getKeywordIds() == null) ? List.of() : body.getKeywordIds();
        keywordService.replaceUserKeywords(userId, ids);
        return ResponseEntity.noContent().build();
    }
}

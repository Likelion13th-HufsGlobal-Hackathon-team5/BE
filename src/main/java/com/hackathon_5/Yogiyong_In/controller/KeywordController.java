// src/main/java/com/hackathon_5/Yogiyong_In/controller/KeywordController.java
package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.KeywordScrollResDto;
import com.hackathon_5.Yogiyong_In.DTO.KeywordUpdateReqDto;
import com.hackathon_5.Yogiyong_In.service.KeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

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
                ✅ 조회도 로그인 필수(Authorization: Bearer ...).
                includeSelected=true면 사용자 선택 여부도 함께 내려줍니다.
                """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @PreAuthorize("isAuthenticated()") // ✅ 조회도 로그인 강제
    @GetMapping("/keywords")
    public ResponseEntity<KeywordScrollResDto> getKeywords(
            @Parameter(description = "이 커서(마지막 keywordId) 이후부터 조회")
            @RequestParam(required = false) Integer cursor,
            @Parameter(description = "가져올 개수(기본 30, 최대 100)")
            @RequestParam(defaultValue = "30") Integer size,
            @Parameter(description = "선택 여부 포함(로그인 필수)")
            @RequestParam(defaultValue = "false") boolean includeSelected
    ) {
        String userId = currentUserIdOrThrow();          // ✅ 항상 로그인 필요
        int s = (size <= 0) ? 30 : Math.min(size, 100);

        // includeSelected는 응답에 선택 여부를 실을지 말지의 토글로만 사용
        boolean effectiveIncludeSelected = includeSelected;

        return ResponseEntity.ok(
                keywordService.getKeywordsScroll(cursor, s, effectiveIncludeSelected, userId)
        );
    }

    @Operation(
            summary = "내 선택 키워드 전체 교체",
            description = """
                전달한 keywordIds만 선택 상태로 저장합니다(전체 교체).
                로그인 사용자 전용입니다.
                expand=names 를 사용하면 ID+이름을 함께 반환합니다.
                """,
            security = { @SecurityRequirement(name = "bearerAuth") }
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
        String userId = currentUserIdOrThrow();

        keywordService.replaceUserKeywords(userId, body.getKeywordIds());

        if ("names".equalsIgnoreCase(expand)) {
            var items = keywordService.getSelectedKeywordsWithNames(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of("userId", userId, "selectedKeywords", items),
                    "error", null
            ));
        } else {
            List<Integer> ids = (body.getKeywordIds() == null) ? List.of() : body.getKeywordIds();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of("userId", userId, "selectedKeywordIds", ids),
                    "error", null
            ));
        }
    }

    @Deprecated
    @Operation(
            summary = "(Admin/Legacy) 특정 유저의 선택 키워드 전체 교체",
            description = "일반 사용자는 자신과 일치하는 userId만 허용됩니다. 신규로는 /api/me/selected-keywords 사용 권장.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "저장 성공(본문 없음)"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 키워드", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/users/{userId}/keywords")
    public ResponseEntity<Void> updateUserKeywords(
            @PathVariable String userId,
            @RequestBody KeywordUpdateReqDto body
    ) {
        String me = currentUserIdOrThrow();
        if (!me.equals(userId)) {
            throw new ResponseStatusException(UNAUTHORIZED, "다른 사용자의 키워드는 수정할 수 없습니다.");
        }
        keywordService.replaceUserKeywords(userId, body.getKeywordIds());
        return ResponseEntity.noContent().build();
    }

    /* ===== 공통 유틸: SecurityContext → userId 추출 ===== */
    private String currentUserIdOrNull() {
        var ctx = SecurityContextHolder.getContext();
        if (ctx == null) return null;
        Authentication auth = ctx.getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object p = auth.getPrincipal();
        if (p == null) return null;

        if (p instanceof String s && !"anonymousUser".equals(s)) return s;
        if (p instanceof UserDetails ud) return ud.getUsername();
        return null;
    }

    private String currentUserIdOrThrow() {
        String uid = currentUserIdOrNull();
        if (uid == null) throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        return uid;
    }
}

package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.dto.ApiResponse;
import com.hackathon_5.Yogiyong_In.dto.Bookmark.BookmarkCreateReqDto;
import com.hackathon_5.Yogiyong_In.dto.Bookmark.BookmarkCreateResDto;
import com.hackathon_5.Yogiyong_In.dto.Bookmark.BookmarkListResDto;
import com.hackathon_5.Yogiyong_In.config.JwtTokenProvider;
import com.hackathon_5.Yogiyong_In.service.BookmarkService;
import com.hackathon_5.Yogiyong_In.util.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookmark", description = "북마크 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "축제 북마크 저장", description = "Body로 festivalId를 받아 북마크를 저장합니다. 이미 있으면 생성하지 않습니다.")
    @PostMapping("/bookmarks")
    public ApiResponse<BookmarkCreateResDto> createBookmark(@RequestBody BookmarkCreateReqDto req,
                                                            HttpServletRequest request) {
        String token = AuthUtils.resolveAccessToken(request);
        if (token == null) return ApiResponse.fail("인증 토큰이 없습니다.");
        String userId = jwtTokenProvider.getSubject(token);

        try {
            BookmarkCreateResDto res = bookmarkService.createBookmark(userId, req.getFestivalId());
            return ApiResponse.ok(res, res.isCreated() ? "북마크가 저장되었습니다." : "이미 저장된 북마크입니다.");
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @Operation(summary = "마이페이지 북마크 모아보기", description = "내가 저장한 북마크 목록을 반환합니다.")
    @GetMapping("/mypage/bookmarks")
    public ApiResponse<BookmarkListResDto> getMyBookmarks(HttpServletRequest request) {
        String token = AuthUtils.resolveAccessToken(request);
        if (token == null) return ApiResponse.fail("인증 토큰이 없습니다.");
        String userId = jwtTokenProvider.getSubject(token);

        try {
            return ApiResponse.ok(bookmarkService.getMyBookmarks(userId), "북마크 목록");
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}

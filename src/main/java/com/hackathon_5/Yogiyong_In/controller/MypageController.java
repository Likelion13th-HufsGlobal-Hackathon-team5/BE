package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.ApiResponse;
import com.hackathon_5.Yogiyong_In.DTO.Mypage.MyPageUserEditReqDto;
import com.hackathon_5.Yogiyong_In.DTO.Mypage.MyPageUserResDto;
import com.hackathon_5.Yogiyong_In.config.JwtTokenProvider;
import com.hackathon_5.Yogiyong_In.service.MypageService;
import com.hackathon_5.Yogiyong_In.util.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Mypage", description = "마이페이지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {

    private final MypageService mypageService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "유저 정보 조회", description = "현재 로그인한 사용자의 기본 정보를 반환합니다.")
    @GetMapping("/user")
    public ApiResponse<MyPageUserResDto> getMyInfo(HttpServletRequest request) {
        String token = AuthUtils.resolveAccessToken(request);
        if (token == null) return ApiResponse.fail("인증 토큰이 없습니다.");
        String userId = jwtTokenProvider.getSubject(token);

        try {
            return ApiResponse.ok(mypageService.getMyInfo(userId), "유저 정보");
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @Operation(summary = "유저 정보 수정", description = "닉네임/출생연도를 수정합니다.")
    @PatchMapping("/user-edit")
    public ApiResponse<MyPageUserResDto> editMyInfo(@RequestBody MyPageUserEditReqDto req,
                                                    HttpServletRequest request) {
        String token = AuthUtils.resolveAccessToken(request);
        if (token == null) return ApiResponse.fail("인증 토큰이 없습니다.");
        String userId = jwtTokenProvider.getSubject(token);

        try {
            return ApiResponse.ok(mypageService.editMyInfo(userId, req), "수정 완료");
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}

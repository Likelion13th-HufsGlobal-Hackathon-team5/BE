package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.dto.ApiResponse;
import com.hackathon_5.Yogiyong_In.dto.Auth.*;
import com.hackathon_5.Yogiyong_In.config.JwtTokenProvider;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserCreateResDto> signup(@Valid @RequestBody UserCreateReqDto req) {
        return ResponseEntity.ok(authService.signup(req));
    }


    //아이디 중복 확인
    @PostMapping("/id-check")
    public ApiResponse<AuthIdCheckResDto> idCheck(@Valid @RequestBody AuthIdCheckReqDto req) {
        return ApiResponse.ok(authService.idCheck(req), "아이디 중복 확인");
    }

    //닉네임 중복 확인
    @PostMapping("/nick-check")
    public ApiResponse<AuthNickCheckResDto> nickCheck(@Valid @RequestBody AuthNickCheckReqDto req) {
        return ApiResponse.ok(authService.nickCheck(req), "닉네임 중복 확인");
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<AuthLoginResDto> login(@Valid @RequestBody AuthLoginReqDto req) {

        // 1. AuthService에서 모든 로직이 처리된 최종 응답 DTO를 받습니다.
        AuthLoginResDto responseDto = authService.login(req);

        // 2. DTO에 포함된 Access Token으로 쿠키를 생성합니다.
        ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", responseDto.getAccessToken())
                .httpOnly(true)
                .secure(false) // HTTPS 적용 시 true로 변경
                .sameSite("Lax")
                .path("/")
                .maxAge(responseDto.getExpiresIn())
                .build();

        // 3. 쿠키는 헤더에 담고, DTO는 본문에 담아 최종 응답을 보냅니다.
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(responseDto);
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie delete = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", delete.toString())
                .body("로그아웃 성공");
    }
}

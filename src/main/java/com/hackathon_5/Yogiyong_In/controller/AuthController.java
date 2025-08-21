package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.ApiResponse;
import com.hackathon_5.Yogiyong_In.DTO.Auth.*;
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

        User user = authService.login(req);

        String accessToken = jwtTokenProvider.createToken(
                user.getUserId(),
                java.util.Map.of("nickname", user.getNickname())
        );

        long maxAge = jwtTokenProvider.getAccessTokenValiditySeconds();

        ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                .httpOnly(true)
                .secure(false) //// 추후 true 로 바꿔야해용
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();

        AuthLoginResDto body = new AuthLoginResDto(
                "Bearer",
                accessToken,
                maxAge,
                user.getUserId(),
                user.getNickname()
        );

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(body);
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

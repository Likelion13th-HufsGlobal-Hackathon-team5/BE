package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.*;
import com.hackathon_5.Yogiyong_In.DTO.Auth.*;
import com.hackathon_5.Yogiyong_In.service.TokenBlacklistService;
import com.hackathon_5.Yogiyong_In.config.JwtTokenProvider;
import com.hackathon_5.Yogiyong_In.service.AuthService;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    //회원가입
    @PostMapping("/signup")
    public ApiResponse<UserCreateResDto> signup(@Valid @RequestBody UserCreateReqDto req) {
        return ApiResponse.ok(authService.signup(req), "회원가입 성공");
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
    public ApiResponse<AuthLoginResDto> login(@Valid @RequestBody AuthLoginReqDto req) {
        return ApiResponse.ok(authService.login(req), "로그인 성공");
    }

    //로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return ApiResponse.ok(null, "이미 로그아웃 되었거나 토큰이 없습니다.");
        }
        String token = header.substring(7);

        var parser = Jwts.parserBuilder().setSigningKey(jwtTokenProvider.getKey()).build();
        var claims = parser.parseClaimsJws(token).getBody();
        Date exp = claims.getExpiration();
        long expiresAt = (exp != null)
                ? exp.getTime()
                : (System.currentTimeMillis() + jwtTokenProvider.getAccessTokenValiditySeconds() * 1000L);

        tokenBlacklistService.blacklist(token, expiresAt);
        return ApiResponse.ok(null, "로그아웃 되었습니다.");
    }
}
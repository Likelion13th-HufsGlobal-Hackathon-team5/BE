// controller/AuthController.java
package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.*;
import com.hackathon_5.Yogiyong_In.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //회원가입
    @PostMapping("/signup")
    public ApiResponse<UserCreateResDto> signup(@Valid @RequestBody UserCreateReqDto req){
        return ApiResponse.ok(authService.signup(req));
    }

    //아이디 중복 확인
    @PostMapping("/id-check")
    public ApiResponse<AuthIdCheckResDto> idCheck(@Valid @RequestBody AuthIdCheckReqDto req){
        return ApiResponse.ok(authService.idCheck(req));
    }

    //닉네임 중복 확인
    @PostMapping("/nick-check")
    public ApiResponse<AuthNickCheckResDto> nickCheck(@Valid @RequestBody AuthNickCheckReqDto req){
        return ApiResponse.ok(authService.nickCheck(req));
    }
}

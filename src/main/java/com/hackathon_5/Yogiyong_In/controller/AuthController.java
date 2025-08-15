package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.*;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
import com.hackathon_5.Yogiyong_In.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    //회원가입
    @PostMapping("/signup")
    public ApiResponse<UserCreateResDto> signup(@Valid @RequestBody UserCreateReqDto req){
        return ApiResponse.ok(authService.signup(req));
    }

    //아이디 중복 확인
    @PostMapping("/id-check")
    public ApiResponse<AuthIdCheckResDto> idCheck(@Valid @RequestBody AuthIdCheckReqDto req) {
        boolean exists = userRepository.existsByUserId(req.getUserId().trim());
        String message = exists ? "이미 존재하는 아이디입니다." : "사용 가능한 아이디입니다.";
        return ApiResponse.ok(new AuthIdCheckResDto(!exists), message);
    }



    //닉네임 중복 확인
    @PostMapping("/nick-check")
    public ApiResponse<AuthNickCheckResDto> nickCheck(@Valid @RequestBody AuthNickCheckReqDto req) {
        boolean exists = userRepository.existsByNickname(req.getNickname().trim());
        String message = exists ? "이미 존재하는 닉네임입니다." : "사용 가능한 닉네임입니다.";
        return ApiResponse.ok(new AuthNickCheckResDto(!exists), message);
    }

}

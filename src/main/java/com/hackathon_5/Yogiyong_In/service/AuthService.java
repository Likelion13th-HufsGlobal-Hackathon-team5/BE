package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.*;
import com.hackathon_5.Yogiyong_In.config.JwtTokenProvider;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public UserCreateResDto signup(UserCreateReqDto req) {
        String userId = req.getUserId() == null ? null : req.getUserId().trim();
        String nickname = req.getNickname() == null ? null : req.getNickname().trim();

        if (!req.getPassword().equals(req.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("아이디를 입력해 주세요.");
        }
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해 주세요.");
        }

        if (userRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .userId(userId)
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(nickname)
                .birthYear(req.getBirthYear())
                .build();

        userRepository.save(user);

        return new UserCreateResDto(user.getUserId());
    }

    // 아이디 중복 확인
    @Transactional(readOnly = true)
    public AuthIdCheckResDto idCheck(AuthIdCheckReqDto req) {
        String userId = req.getUserId().trim();
        boolean exists = userRepository.existsByUserId(userId);
        return new AuthIdCheckResDto(!exists);
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public AuthNickCheckResDto nickCheck(AuthNickCheckReqDto req) {
        String nickname = req.getNickname().trim();
        boolean exists = userRepository.existsByNickname(nickname);
        return new AuthNickCheckResDto(!exists);
    }

    // 로그인
    @Transactional(readOnly = true)
    public AuthLoginResDto login(AuthLoginReqDto req) {
        String userId = req.getUserId() == null ? null : req.getUserId().trim();

        var user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(
                user.getUserId(),
                Map.of("nickname", user.getNickname())
        );

        return new AuthLoginResDto(
                "Bearer",
                token,
                jwtTokenProvider.getAccessTokenValiditySeconds(),
                user.getUserId(),
                user.getNickname()
        );
    }
}

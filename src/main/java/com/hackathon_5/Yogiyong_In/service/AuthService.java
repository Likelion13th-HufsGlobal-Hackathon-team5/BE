package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.dto.Auth.*;
import com.hackathon_5.Yogiyong_In.config.JwtTokenProvider;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.repository.UserKeywordRepository;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserKeywordRepository userKeywordRepository;

    // 회원가입
    @Transactional
    public UserCreateResDto signup(UserCreateReqDto req) {
        String userId = req.getUserId().trim();
        String nickname = req.getNickname().trim();

        // 1) 아이디/닉네임 중복확인용 토큰 검증
        try {
            if (req.getIdCheckToken() == null || req.getNickCheckToken() == null) {
                throw new IllegalArgumentException("회원가입 전에 아이디와 닉네임 중복 확인을 해주세요.");
            }

            Claims idClaims = Jwts.parserBuilder()
                    .setSigningKey(jwtTokenProvider.getKey())
                    .build()
                    .parseClaimsJws(req.getIdCheckToken())
                    .getBody();
            if (!userId.equals(idClaims.get("userId", String.class))) {
                throw new IllegalArgumentException("아이디 중복 확인 토큰이 유효하지 않습니다.");
            }

            Claims nickClaims = Jwts.parserBuilder()
                    .setSigningKey(jwtTokenProvider.getKey())
                    .build()
                    .parseClaimsJws(req.getNickCheckToken())
                    .getBody();
            if (!nickname.equals(nickClaims.get("nickname", String.class))) {
                throw new IllegalArgumentException("닉네임 중복 확인 토큰이 유효하지 않습니다.");
            }
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("중복 확인 후 시간이 초과되었습니다. 다시 확인해주세요.");
        } catch (JwtException e) {
            throw new IllegalArgumentException("유효하지 않은 중복 확인 토큰입니다.");
        }

        // 2) 비밀번호 확인
        if (!req.getPassword().equals(req.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 3) 최종 중복 확인(DB 레벨)
        if (userRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 4) 저장
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

        if (exists) {
            return new AuthIdCheckResDto(false, "이미 사용 중인 아이디입니다.", null);
        }

        String token = jwtTokenProvider.createCheckToken(userId, "userId", 300);
        return new AuthIdCheckResDto(true, "사용 가능한 아이디입니다.", token);
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public AuthNickCheckResDto nickCheck(AuthNickCheckReqDto req) {
        String nickname = req.getNickname().trim();
        boolean exists = userRepository.existsByNickname(nickname);

        if (exists) {
            return new AuthNickCheckResDto(false, "이미 사용 중인 닉네임입니다.", null);
        }

        String token = jwtTokenProvider.createCheckToken(nickname, "nickname", 500);
        return new AuthNickCheckResDto(true, "사용 가능한 닉네임입니다.", token);
    }

    // 로그인 검증 (쿠키 방식: 토큰 발급/반환은 컨트롤러에서 수행)
    @Transactional(readOnly = true)
    public AuthLoginResDto login(AuthLoginReqDto req) {
        String userId = req.getUserId().trim();

        // 1. 사용자 인증
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 2. AI 키워드 사용 이력 확인
        // 괄호 오타를 수정했습니다.
        boolean hasHistory = userKeywordRepository.existsByUser_UserIdAndIsSelectedTrue(userId);

        // 3. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        long expiresIn = jwtTokenProvider.getAccessTokenValidityInSeconds();

        // 4. 최종 응답 DTO 생성하여 반환
        return AuthLoginResDto.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .hasAiKeywordHistory(hasHistory) // 확인된 이력 정보 추가
                .build();
    }

}

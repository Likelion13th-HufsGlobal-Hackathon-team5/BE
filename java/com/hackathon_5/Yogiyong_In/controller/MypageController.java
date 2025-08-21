package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.Mypage.MyPageUserEditReqDto;
import com.hackathon_5.Yogiyong_In.config.JwtTokenProvider;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

@Tag(name = "Mypage", description = "마이페이지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Operation(summary = "유저 정보 수정", description = "name/birthyear 필수, password는 선택. 성공 시 message만 반환.")
    @PatchMapping("/user-edit")
    public ResponseEntity<?> editMyInfo(@Valid @RequestBody MyPageUserEditReqDto req,
                                        HttpServletRequest request) {

        // 1) 인증 확인
        String token = resolveAccessToken(request);
        if (!StringUtils.hasText(token)) {
            return ResponseEntity.status(401).body(Map.of("message", "인증이 필요합니다."));
        }

        String userId;
        try {
            userId = jwtTokenProvider.getSubject(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "인증이 필요합니다."));
        }

        // 2) 유저 조회
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "인증이 필요합니다."));
        }
        User user = optionalUser.get();

        // 3) 추가 검증 (password 관련)
        //    - password가 들어왔을 때만 유효성 검증
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        if (StringUtils.hasText(req.getPassword())) {
            // 비밀번호 패턴: 영문/숫자/특수문자 포함 8자+
            Pattern pwPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,}$");
            if (!pwPattern.matcher(req.getPassword()).matches()) {
                fieldErrors.put("password", List.of("비밀번호는 영문/숫자/특수문자를 포함하여 8자 이상이어야 합니다."));
            }
            if (!Objects.equals(req.getPassword(), req.getPasswordConfirm())) {
                fieldErrors.put("passwordConfirm", List.of("비밀번호가 일치하지 않습니다."));
            }
        }

        // birthyear → Integer 변환 (숫자 아님/범위 오류도 400 처리)
        Integer birthYear = null;
        try {
            birthYear = Integer.parseInt(req.getBirthyear());
        } catch (NumberFormatException e) {
            // 스펙상의 메시지를 그대로 사용
            fieldErrors.putIfAbsent("birthyear", List.of("이 필드는 필수입니다."));
        }

        // 닉네임 중복 방지 (DB unique 제약 보호)
        if (!user.getNickname().equals(req.getName()) && userRepository.existsByNickname(req.getName())) {
            fieldErrors.put("name", List.of("이미 사용 중인 닉네임입니다."));
        }

        if (!fieldErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(fieldErrors);
        }

        // 4) 업데이트 적용
        user.updateProfile(req.getName(), birthYear, req.getProfileImageUrl());
        if (StringUtils.hasText(req.getPassword())) {
            // ※ 비밀번호 인코딩이 필요하다면 여기에서 encoder.encode(...) 적용
            user.changePassword(req.getPassword());
        }
        userRepository.save(user);

        // 5) 성공 응답 (스펙대로 message만)
        return ResponseEntity.ok(Map.of("message", "유저 정보가 수정되었습니다."));
    }

    // ------- 내부 유틸: 토큰 추출 (Cookie ACCESS_TOKEN → Authorization 순) -------
    private String resolveAccessToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("ACCESS_TOKEN".equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    return c.getValue();
                }
            }
        }
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}

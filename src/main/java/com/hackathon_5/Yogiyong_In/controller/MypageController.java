package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.dto.Mypage.MyPageUserEditReqDto;
import com.hackathon_5.Yogiyong_In.dto.Mypage.MyPageUserResDto;
import com.hackathon_5.Yogiyong_In.config.JwtTokenProvider;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
import com.hackathon_5.Yogiyong_In.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Tag(name = "Mypage", description = "마이페이지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final MypageService mypageService;

    @Operation(summary = "내 정보 조회", description = "로그인 사용자의 프로필 정보를 반환합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MyPageUserResDto.class))
    )
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MyPageUserResDto> getMyInfo() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String userId = (auth != null) ? (String) auth.getPrincipal() : null;
        if (userId == null) {
            return ResponseEntity.status(401).<MyPageUserResDto>build();
        }
        return ResponseEntity.ok(mypageService.getMyInfo(userId));
    }

    @Operation(summary = "유저 정보 수정", description = "name/birthyear 필수, password는 선택. 성공 시 message만 반환.")
    @PatchMapping("/user-edit")
    public ResponseEntity<?> editMyInfo(@Valid @RequestBody MyPageUserEditReqDto req,
                                        BindingResult bindingResult,
                                        HttpServletRequest request) {

        // 1) DTO 유효성 검사 오류 처리
        if (bindingResult.hasErrors()) {
            Map<String, List<String>> fieldErrors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.groupingBy(
                            FieldError::getField,
                            Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                    ));
            return ResponseEntity.badRequest().body(fieldErrors);
        }

        // 2) 인증 확인
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

        // 3) 유저 조회
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "인증이 필요합니다."));
        }
        User user = optionalUser.get();

        // 4) 추가 검증 (수동 처리)
        Map<String, List<String>> manualFieldErrors = new LinkedHashMap<>();

        // 비밀번호 유효성 검증
        if (StringUtils.hasText(req.getPassword())) {
            // 비밀번호 패턴: 영문/숫자/특수문자 포함 8자+
            Pattern pwPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,}$");
            if (!pwPattern.matcher(req.getPassword()).matches()) {
                manualFieldErrors.put("password", List.of("비밀번호는 영문/숫자/특수문자를 포함하여 8자 이상이어야 합니다."));
            }
            if (!Objects.equals(req.getPassword(), req.getPasswordConfirm())) {
                manualFieldErrors.put("passwordConfirm", List.of("비밀번호가 일치하지 않습니다."));
            }
        }

        // 닉네임 중복 방지
        if (req.getNickname() != null && !user.getNickname().equals(req.getNickname())) {
            if (userRepository.existsByNickname(req.getNickname())) {
                manualFieldErrors.put("nickname", List.of("이미 사용 중인 닉네임입니다."));
            }
        }

        if (!manualFieldErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(manualFieldErrors);
        }

        // 5) 업데이트 적용
        user.updateProfile(req.getNickname(), req.getBirthyear());
        if (StringUtils.hasText(req.getPassword())) {
            user.changePassword(req.getPassword());
        }
        userRepository.save(user);

        // 6) 성공 응답
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

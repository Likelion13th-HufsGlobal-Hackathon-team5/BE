package com.hackathon_5.Yogiyong_In.service;


import com.hackathon_5.Yogiyong_In.DTO.*;
import com.hackathon_5.Yogiyong_In.domain.*;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
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

    // 회원가입
    @Transactional
    public UserCreateResDto signup(UserCreateReqDto req){
        if (!req.getPassword().equals(req.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        if (userRepository.existsByUserId(req.getUserId()))
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        if (userRepository.existsByNickname(req.getNickname()))
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");

        User user = User.builder()
                .userId(req.getUserId())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .birthYear(req.getBirthYear())
                .build();

        userRepository.save(user);
        return new UserCreateResDto(user.getUserId());
    }

    //아이디 중복 확인
    @Transactional(readOnly = true)
    public AuthIdCheckResDto idCheck(AuthIdCheckReqDto req) {
        String userId = req.getUserId().trim();
        boolean exists = userRepository.existsByUserId(userId);

        return new AuthIdCheckResDto(!exists);
    }




    //닉네임 중복 확인
    @Transactional(readOnly = true)
    public AuthNickCheckResDto nickCheck(AuthNickCheckReqDto req) {
        boolean exists = userRepository.existsByNickname(req.getNickname().trim());
        return new AuthNickCheckResDto(!exists);
    }

}


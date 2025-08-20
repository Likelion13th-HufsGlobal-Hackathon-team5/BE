package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.Mypage.MyPageUserEditReqDto;
import com.hackathon_5.Yogiyong_In.DTO.Mypage.MyPageUserResDto;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MypageService {

    private final UserRepository userRepository;

    public MyPageUserResDto getMyInfo(String userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return MyPageUserResDto.from(u);
    }

    public MyPageUserResDto editMyInfo(String userId, MyPageUserEditReqDto req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (req.getNickname() != null && !req.getNickname().equals(u.getNickname())) {
            if (userRepository.existsByNickname(req.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }

        updateUser(u, req.getNickname(), req.getBirthYear());

        return MyPageUserResDto.from(u);
    }

    private void updateUser(User u, String nickname, Integer birthYear) {
        try {
            var nickField = User.class.getDeclaredField("nickname");
            nickField.setAccessible(true);
            if (nickname != null) nickField.set(u, nickname);

            var birthField = User.class.getDeclaredField("birthYear");
            birthField.setAccessible(true);
            if (birthYear != null) birthField.set(u, birthYear);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("유저 정보 수정 중 오류가 발생했습니다.", e);
        }
    }
}

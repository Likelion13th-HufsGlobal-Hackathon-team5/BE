package com.hackathon_5.Yogiyong_In.dto.Mypage;

import com.hackathon_5.Yogiyong_In.domain.User;
import lombok.*;

@Getter @AllArgsConstructor @Builder
public class MyPageUserResDto {
    private String userId;
    private String nickname;
    private Integer birthYear;

    public static MyPageUserResDto from(User u) {
        return MyPageUserResDto.builder()
                .userId(u.getUserId())
                .nickname(u.getNickname())
                .birthYear(u.getBirthYear())
                .build();
    }
}
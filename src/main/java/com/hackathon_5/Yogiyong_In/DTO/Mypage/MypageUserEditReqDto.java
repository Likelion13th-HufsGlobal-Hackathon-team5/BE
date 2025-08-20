package com.hackathon_5.Yogiyong_In.DTO.Mypage;

import com.hackathon_5.Yogiyong_In.domain.User;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MyPageUserEditReqDto {
    private String nickname;
    private Integer birthYear;
}
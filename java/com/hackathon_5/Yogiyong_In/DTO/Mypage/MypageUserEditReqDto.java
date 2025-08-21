package com.hackathon_5.Yogiyong_In.DTO.Mypage;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class MyPageUserEditReqDto {

    @NotBlank(message = "이 필드는 필수입니다.")
    private String name;

    @NotBlank(message = "이 필드는 필수입니다.")
    private String birthyear;

    private String password;        // 선택 입력
    private String passwordConfirm; // 선택 입력
    private String profileImageUrl; // 선택 입력
}

package com.hackathon_5.Yogiyong_In.DTO.Mypage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class MyPageUserEditReqDto {

    @NotBlank(message = "이 필드는 필수입니다.")
    private String nickname;

    @NotNull(message = "이 필드는 필수입니다.")
    private Integer birthyear;

    private String password;
    private String passwordConfirm;
    private String profileImageUrl;
}
package com.hackathon_5.Yogiyong_In.DTO.Auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateReqDto {

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Size(min = 8, max = 100, message = "아이디는 8자 이상 100자 이하로 입력해주세요.")
    private String userId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    private Integer birthYear;

    // 추가된 필드
    @NotBlank(message = "아이디 중복 확인을 해주세요.")
    private String idCheckToken;

    @NotBlank(message = "닉네임 중복 확인을 해주세요.")
    private String nickCheckToken;
}
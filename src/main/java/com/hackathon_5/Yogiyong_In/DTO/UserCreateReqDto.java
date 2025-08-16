package com.hackathon_5.Yogiyong_In.DTO;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCreateReqDto {

    @NotBlank
    @Size(min = 8, max = 100, message = "아이디는 최소 8자입니다.")
    private String userId;

    @NotBlank
    @Size(min = 8, max = 100, message = "비밀번호는 최소 8자입니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인이 필요합니다")
    private String passwordConfirm;

    @NotBlank
    @Size(max = 20)
    private String nickname;

    @Min(1900) @Max(2100)
    private Integer birthYear;
}
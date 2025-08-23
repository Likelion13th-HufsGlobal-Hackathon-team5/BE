package com.hackathon_5.Yogiyong_In.dto.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthLoginReqDto {
    @NotBlank
    private String userId;

    @NotBlank
    private String password;
}

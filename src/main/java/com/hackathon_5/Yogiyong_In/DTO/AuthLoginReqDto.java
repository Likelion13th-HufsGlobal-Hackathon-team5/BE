package com.hackathon_5.Yogiyong_In.DTO;

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

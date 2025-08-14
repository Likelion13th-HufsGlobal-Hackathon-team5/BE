package com.hackathon_5.Yogiyong_In.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthIdCheckReqDto {
    @NotBlank
    @Size(min = 8, max = 100)
    private String userId;
}


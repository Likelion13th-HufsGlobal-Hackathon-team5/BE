package com.hackathon_5.Yogiyong_In.DTO.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthIdCheckResDto {
    private boolean available;
    private String message;
    private String token;
}

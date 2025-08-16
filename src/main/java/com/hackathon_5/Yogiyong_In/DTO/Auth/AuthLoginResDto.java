package com.hackathon_5.Yogiyong_In.DTO.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthLoginResDto {
    private String tokenType;
    private String accessToken;
    private long   expiresIn;
    private String userId;
    private String nickname;
}

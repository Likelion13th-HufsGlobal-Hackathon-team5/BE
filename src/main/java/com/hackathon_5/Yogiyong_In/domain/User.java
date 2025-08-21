package com.hackathon_5.Yogiyong_In.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "password", length = 200, nullable = false)
    private String password;

    @Column(name = "nickname", length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Builder
    public User(String userId, String password, String nickname, Integer birthYear, String profileImageUrl) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.birthYear = birthYear;
        this.profileImageUrl = profileImageUrl;
    }
    public void updateProfile(String nickname, Integer birthYear, String profileImageUrl) {
        if (nickname != null) this.nickname = nickname;
        if (birthYear != null) this.birthYear = birthYear;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
}

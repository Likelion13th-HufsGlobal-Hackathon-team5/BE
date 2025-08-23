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


    @Builder
    public User(String userId, String password, String nickname, Integer birthYear) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.birthYear = birthYear;
    }
    public void updateProfile(String nickname, Integer birthYear) {
        if (nickname != null) this.nickname = nickname;
        if (birthYear != null) this.birthYear = birthYear;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
}

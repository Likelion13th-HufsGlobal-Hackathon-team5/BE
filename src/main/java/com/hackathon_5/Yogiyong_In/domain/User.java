package com.hackathon_5.Yogiyong_In.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users") // 스키마와 동일
public class User {

    @Id
    @Column(name = "user_id", length = 20)
    private String userId;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(nullable = false, length = 20, unique = true)
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
}


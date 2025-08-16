package com.hackathon_5.Yogiyong_In.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity @Table(name = "User")
public class User {

    @Id
    @Column(length = 36)
    private String userId;


    @PrePersist
    public void generateId() {
        if (this.userId == null) {
            this.userId = UUID.randomUUID().toString();
        }
    }


    @Column(nullable = false, length = 200)
    private String password;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    private Integer birthYear;

    @Builder
    public User(String userId, String password, String nickname, Integer birthYear) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.birthYear = birthYear;
    }
}

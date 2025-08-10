package com.hackathon_5.Yogiyong_In.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter@Setter
@NoArgsConstructor
public class User {
    @Id
    public String userId;
    public String password;
    public String nickname;
    public int birthYear;

}

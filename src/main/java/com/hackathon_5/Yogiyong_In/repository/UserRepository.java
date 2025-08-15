package com.hackathon_5.Yogiyong_In.repository;

import com.hackathon_5.Yogiyong_In.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    //회원가입
    boolean existsByUserId(String userId);
    boolean existsByNickname(String nickname);

    //로그인
    Optional<User> findByUserId(String userId);
}


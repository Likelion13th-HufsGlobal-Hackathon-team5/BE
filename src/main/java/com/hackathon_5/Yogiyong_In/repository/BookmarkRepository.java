package com.hackathon_5.Yogiyong_In.repository;

import com.hackathon_5.Yogiyong_In.domain.Bookmark;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndFestival(User user, Festival festival);
    Optional<Bookmark> findByUserAndFestival(User user, Festival festival);
    List<Bookmark> findAllByUser(User user);
}

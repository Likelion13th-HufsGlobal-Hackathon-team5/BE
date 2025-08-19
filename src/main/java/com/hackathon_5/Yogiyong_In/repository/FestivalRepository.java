package com.hackathon_5.yogiyong_in.repository;

import com.hackathon_5.yogiyong_in.domain.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {
}

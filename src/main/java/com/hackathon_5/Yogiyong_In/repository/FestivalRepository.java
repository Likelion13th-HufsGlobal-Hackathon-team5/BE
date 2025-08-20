package com.hackathon_5.Yogiyong_In.repository;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Integer> {
    List<Festival> findByFestivalStartLessThanEqualAndFestivalEndGreaterThanEqual(
            String startDate, String endDate
    );
}

package com.hackathon_5.Yogiyong_In.repository;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {
    // 특정 날짜가 startDate ~ endDate 범위에 포함되는 축제 조회
    List<Festival> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(String date1, String date2);
}

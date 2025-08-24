package com.hackathon_5.Yogiyong_In.repository;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.dto.Festival.FestivalCalendarDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Integer> {

    // --- 기존 메소드들은 그대로 유지 ---
    List<Festival> findByFestivalStartLessThanEqualAndFestivalEndGreaterThanEqual(
            LocalDate startDate, LocalDate endDate
    );

    @Query("""
      select new com.hackathon_5.Yogiyong_In.dto.Festival.FestivalCalendarDto(
         f.festivalId, f.festivalStart, f.festivalEnd
      )
      from Festival f
      where f.festivalStart <= :end
        and f.festivalEnd   >= :start
      order by f.festivalStart asc
    """)
    List<FestivalCalendarDto> findForCalendar(
            @Param("start") LocalDate start,
            @Param("end")   LocalDate end
    );

    // ✅✅✅ 이 부분을 추가해주세요! ✅✅✅
    // 특정 키워드 ID를 가진 축제 목록을 조회합니다.
    List<Festival> findByKeywords_KeywordId(Integer keywordId);
}

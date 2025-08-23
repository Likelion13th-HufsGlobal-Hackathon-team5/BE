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

    // 기존 메서드 ｓｔｒｉｎｇ 사용하면 데이터타입 안맞아서 이 부분 수정함
    List<Festival> findByFestivalStartLessThanEqualAndFestivalEndGreaterThanEqual(
            LocalDate startDate, LocalDate endDate
    );

    // 커스텀 JPQL
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
}

package com.hackathon_5.Yogiyong_In.repository;

import com.hackathon_5.Yogiyong_In.domain.Keyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Integer> {

    @Query("""
      select k
        from Keyword k
       where (:cursor is null or k.keywordId > :cursor)
       order by k.keywordId asc
    """)
    List<Keyword> findScroll(@Param("cursor") Integer cursor, Pageable pageable);

    // ✅ 추가
    List<Keyword> findByKeywordIdIn(List<Integer> keywordIds);
}

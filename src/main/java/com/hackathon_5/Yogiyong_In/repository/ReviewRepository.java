package com.hackathon_5.Yogiyong_In.repository;

import com.hackathon_5.Yogiyong_In.domain.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("""
      select r
        from Review r
       where r.festival.festivalId = :festivalId
         and (:cursor is null or r.reviewId > :cursor)
       order by r.reviewId asc
    """)
    List<Review> findScrollByFestival(
            @Param("festivalId") Integer festivalId,
            @Param("cursor") Integer cursor,
            Pageable pageable
    );
}

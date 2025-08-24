package com.hackathon_5.Yogiyong_In.repository;

import com.hackathon_5.Yogiyong_In.domain.Bookmark;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.dto.Festival.PopularFestivalDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndFestival(User user, Festival festival);
    Optional<Bookmark> findByUserAndFestival(User user, Festival festival);
    List<Bookmark> findAllByUser(User user);

    @Query("""
        select new com.hackathon_5.Yogiyong_In.dto.Festival.PopularFestivalDto(
            f.festivalId, f.festivalName, f.festivalDesc,
            f.festivalStart, f.festivalEnd, f.festivalLoca, f.imagePath,
            count(b)
        )
        from Bookmark b
        join b.festival f
        group by f.festivalId, f.festivalName, f.festivalDesc,
                 f.festivalStart, f.festivalEnd, f.festivalLoca, f.imagePath
        order by count(b) desc, f.festivalStart desc
    """)
    List<PopularFestivalDto> findPopularFestivals(Pageable pageable);
}

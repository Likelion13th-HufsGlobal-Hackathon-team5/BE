package com.hackathon_5.Yogiyong_In.repository;

import com.hackathon_5.Yogiyong_In.domain.UserKeyword;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Integer> {

    // 현재 페이지에 뜬 키워드들 중, 해당 유저가 선택한 것만 조회
    @Query("""
      select uk from UserKeyword uk
       where uk.user.userId = :userId
         and uk.keyword.keywordId in :keywordIds
         and uk.isSelected = true
    """)
    List<UserKeyword> findSelectedIn(
            @Param("userId") String userId,
            @Param("keywordIds") Collection<Integer> keywordIds
    );

    // 해당 유저의 기존 선택을 전부 false로
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update UserKeyword uk
         set uk.isSelected = false
       where uk.user.userId = :userId
         and uk.isSelected = true
    """)
    int clearSelections(@Param("userId") String userId);

    // 단일 키워드 선택 여부
    Optional<UserKeyword> findByUser_UserIdAndKeyword_KeywordId(String userId, Integer keywordId);

    // 현재 페이지 키워드들 중 선택된 것들
    List<UserKeyword> findByUser_UserIdAndKeyword_KeywordIdIn(String userId, Collection<Integer> keywordIds);

    // ✅ 추가: 해당 유저의 "현재 선택된" 모든 키워드 (expand=names에서 사용)
    List<UserKeyword> findByUser_UserIdAndIsSelectedTrue(String userId);
}

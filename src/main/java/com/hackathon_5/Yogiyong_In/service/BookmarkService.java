package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.dto.Bookmark.*;
import com.hackathon_5.Yogiyong_In.domain.Bookmark;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.repository.BookmarkRepository;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final FestivalRepository festivalRepository;

    public BookmarkCreateResDto createBookmark(String userId, Integer festivalId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));

        return bookmarkRepository.findByUserAndFestival(user, festival)
                .map(b -> new BookmarkCreateResDto(false, b.getId())) // 이미 있으면 false
                .orElseGet(() -> {
                    Bookmark saved = bookmarkRepository.save(Bookmark.builder()
                            .user(user)
                            .festival(festival)
                            .build());
                    return new BookmarkCreateResDto(true, saved.getId());
                });
    }

    public BookmarkListResDto getMyBookmarks(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        List<Bookmark> list = bookmarkRepository.findAllByUser(user);

        List<BookmarkItemResDto> items = list.stream()
                .map(b -> BookmarkItemResDto.builder()
                        .bookmarkId(b.getId())
                        .festival(BookmarkFestivalDto.from(b.getFestival()))
                        .build()
                ).toList();

        return BookmarkListResDto.builder()
                .count(items.size())
                .items(items)
                .build();
    }

    public void deleteBookmark(String userId, Integer festivalId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));

        Bookmark bookmark = bookmarkRepository.findByUserAndFestival(user, festival)
                .orElseThrow(() -> new IllegalArgumentException("해당 북마크가 존재하지 않습니다."));

        bookmarkRepository.delete(bookmark);
    }
}

package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.dto.Festival.FestivalCalendarDto;
import com.hackathon_5.Yogiyong_In.dto.Festival.FestivalInfoResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.dto.Festival.PopularFestivalDto;
import com.hackathon_5.Yogiyong_In.repository.BookmarkRepository;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final FestivalRepository festivalRepository;
    private final BookmarkRepository bookmarkRepository;
    private final FestivalAiReviewService festivalAiReviewService;

    // 목록보기
    public List<FestivalInfoResDto> getAllFestivals() {
        return getAllFestivals(false);
    }

    public List<FestivalInfoResDto> getAllFestivals(boolean withAi) {
        return festivalRepository.findAll().stream()
                .map(f -> toResDTO(f, withAi))
                .collect(Collectors.toList());
    }

    // 상세보기
    public Optional<FestivalInfoResDto> getFestivalById(Integer id) {
        return getFestivalById(id, true);
    }

    public Optional<FestivalInfoResDto> getFestivalById(Integer id, boolean withAi) {
        return festivalRepository.findById(id)
                .map(f -> toResDTO(f, withAi));
    }

    // 특정 날짜 포함 축제 조회
    public List<FestivalInfoResDto> getFestivalsByDate(int year, int month, int date) {
        return getFestivalsByDate(year, month, date, false);
    }

    public List<FestivalInfoResDto> getFestivalsByDate(int year, int month, int date, boolean withAi) {
        LocalDate targetDate = LocalDate.of(year, month, date);
        return festivalRepository
                .findByFestivalStartLessThanEqualAndFestivalEndGreaterThanEqual(targetDate, targetDate)
                .stream()
                .map(f -> toResDTO(f, withAi))
                .collect(Collectors.toList());
    }

    // 월별 조회(달력용)
    public List<FestivalCalendarDto> getByMonth(int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd   = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        return festivalRepository.findForCalendar(monthStart, monthEnd)
                .stream()
                .map(festival -> FestivalCalendarDto.builder()
                        .festivalId(festival.getFestivalId())
                        .festivalStart(festival.getFestivalStart())
                        .festivalEnd(festival.getFestivalEnd())
                        .displayStart(festival.getFestivalStart().isBefore(monthStart) ? monthStart : festival.getFestivalStart())
                        .displayEnd(festival.getFestivalEnd().isAfter(monthEnd) ? monthEnd : festival.getFestivalEnd())
                        .build()
                )
                .collect(Collectors.toList());
    }

    //범위 조회(달력용)
    public List<FestivalCalendarDto> getRange(LocalDate start, LocalDate end) {
        return festivalRepository.findForCalendar(start, end)
                .stream()
                .map(festival -> FestivalCalendarDto.builder()
                        .festivalId(festival.getFestivalId())
                        .festivalStart(festival.getFestivalStart())
                        .festivalEnd(festival.getFestivalEnd())
                        .displayStart(festival.getFestivalStart().isBefore(start) ? start : festival.getFestivalStart())
                        .displayEnd(festival.getFestivalEnd().isAfter(end) ? end : festival.getFestivalEnd())
                        .build()
                )
                .collect(Collectors.toList());
    }

    // 공통 DTO 매핑
    private FestivalInfoResDto toResDTO(Festival festival, boolean withAi) {
        String ai = null;
        if (withAi) {
            try {
                ai = festivalAiReviewService.generateFestivalReview(festival.getFestivalDesc());
            } catch (Exception e) {
                ai = "AI 리뷰 생성에 실패했습니다.";
            }
        }

        return FestivalInfoResDto.builder()
                .festivalId(festival.getFestivalId())
                .festivalName(festival.getFestivalName())
                .festivalDesc(festival.getFestivalDesc())
                .festivalStart(festival.getFestivalStart())
                .festivalEnd(festival.getFestivalEnd())
                .festivalLoca(festival.getFestivalLoca())
                .imagePath(festival.getImagePath())
                .aiReview(ai)
                .build();
    }

    //전기간 기준 북마크 TOP 5 조회
    public List<PopularFestivalDto> getPopularTop5() {
        return bookmarkRepository.findPopularFestivals(PageRequest.of(0, 5));
    }
}

package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.Festival.FestivalCalendarDto;
import com.hackathon_5.Yogiyong_In.DTO.Festival.FestivalInfoResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
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

    public List<FestivalInfoResDto> getAllFestivals() {
        return festivalRepository.findAll().stream()
                .map(this::toResDTO)
                .collect(Collectors.toList());
    }

    public Optional<FestivalInfoResDto> getFestivalById(Integer id) {
        return festivalRepository.findById(id)
                .map(this::toResDTO);
    }

    public List<FestivalInfoResDto> getFestivalsByDate(int year, int month, int date) {
        LocalDate targetDate = LocalDate.of(year, month, date);
        return festivalRepository.findByFestivalStartLessThanEqualAndFestivalEndGreaterThanEqual(
                        targetDate, targetDate
                )
                .stream()
                .map(this::toResDTO)
                .collect(Collectors.toList());
    }

    // === 월별 조회 ===
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

    // === 범위 조회 ===
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

    private FestivalInfoResDto toResDTO(Festival festival) {
        return FestivalInfoResDto.builder()
                .festivalId(festival.getFestivalId())
                .festivalName(festival.getFestivalName())
                .festivalDesc(festival.getFestivalDesc())
                .festivalStart(festival.getFestivalStart())
                .festivalEnd(festival.getFestivalEnd())
                .festivalLoca(festival.getFestivalLoca())
                .imagePath(festival.getImagePath())
                // .aiReview(festival.getAiReview())
                .build();
    }
}

package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarService {

    private final FestivalRepository festivalRepository;

    public CalendarService(FestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    public List<Festival> getFestivalByDate(int year, int month, int date) {
        LocalDate targetDate = LocalDate.of(year, month, date);
        return festivalRepository.findByFestivalStartLessThanEqualAndFestivalEndGreaterThanEqual(
                targetDate, targetDate
        );
    }
}

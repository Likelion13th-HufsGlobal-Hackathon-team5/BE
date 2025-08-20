package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.Festival.FestivalInfoResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public ResponseEntity<List<FestivalInfoResDto>> getAllFestivals() {
        return ResponseEntity.ok(calendarService.getAllFestivals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FestivalInfoResDto> getFestivalById(@PathVariable Integer id) {
        return calendarService.getFestivalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = {"year", "month", "date"})
    public ResponseEntity<List<FestivalInfoResDto>> getFestivalsByDate(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int date) {
        return ResponseEntity.ok(calendarService.getFestivalByDate(year, month, date));
    }
}

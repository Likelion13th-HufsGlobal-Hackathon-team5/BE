package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.dto.Festival.FestivalInfoResDto;
import com.hackathon_5.Yogiyong_In.dto.Festival.FestivalCalendarDto;
import com.hackathon_5.Yogiyong_In.dto.ApiResponse;
import com.hackathon_5.Yogiyong_In.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

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
        return ResponseEntity.ok(calendarService.getFestivalsByDate(year, month, date));
    }

    @Operation(summary = "월별 축제 조회(달력용)")
    @GetMapping("/by-month")
    public ResponseEntity<ApiResponse<List<FestivalCalendarDto>>> byMonth(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(calendarService.getByMonth(year, month)));
    }

    @Operation(summary = "기간 겹침 축제 조회(달력용)")
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<FestivalCalendarDto>>> byRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.ok(calendarService.getRange(start, end)));
    }
}

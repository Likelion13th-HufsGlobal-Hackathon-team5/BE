package com.hackathon_5.Yogiyong_In.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CalendarGetResDto {
    private Long festivalId;
    private String festivalName;
    private String festivalDescription;
    private String startDate;   // or LocalDate
    private String endDate;     // or LocalDate
    private String location;
    private String imagePath;
}

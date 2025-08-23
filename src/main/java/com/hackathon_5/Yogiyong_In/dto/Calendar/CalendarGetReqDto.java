package com.hackathon_5.Yogiyong_In.dto.Calendar;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CalendarGetReqDto {

    @Min(1900) @Max(2100)
    private int year;

    @Min(1) @Max(12)
    private int month;

    @Min(1) @Max(31)
    private int date;
}

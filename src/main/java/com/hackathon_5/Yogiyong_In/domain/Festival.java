package com.hackathon_5.Yogiyong_In.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long festivalId;

    private String name;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private String location;
    private String imagePath;
}
package com.hackathon_5.yogiyong_in.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long festivalId;

    private String festivalName;
    private String startDate;
    private String endDate;
    private String location;
    private String description;
    private String imagePath;
    private String aiReview;
}

package com.hackathon_5.Yogiyong_In.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keywords")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id", nullable = false)
    private Integer keywordId; // ERD: INT

    @Column(name = "keyword_name", length = 50, nullable = false)
    private String keywordName;

    @Column(name = "slug", nullable = false, unique = true, length = 32)
    private String slug;
}

package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.FestivalInfoReqDTO;
import com.hackathon_5.Yogiyong_In.DTO.FestivalInfoResDTO;
import com.hackathon_5.Yogiyong_In.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/festivals")
@RequiredArgsConstructor

public class FestivalController {

    private final FestivalService festivalService;

    @GetMapping
    public ResponseEntity<List<FestivalInfoResDTO>> getAllFestivals() {
        return ResponseEntity.ok(festivalService.getAllFestivals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FestivalInfoResDTO> getFestivalById(@PathVariable Long id) {
        return festivalService.getFestivalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FestivalInfoResDTO> createFestival(@RequestBody FestivalInfoReqDTO reqDTO) {
        return ResponseEntity.ok(festivalService.saveFestival(reqDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFestival(@PathVariable Long id) {
        festivalService.deleteFestival(id);
        return ResponseEntity.noContent().build();
    }
}

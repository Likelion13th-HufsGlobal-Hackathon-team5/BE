package com.hackathon_5.Yogiyong_In.service.

import com.hackathon_5.yogiyong_in.domain.Festival;
import com.hackathon_5.yogiyong_in.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public List<Festival> getAllFestivals() {
        return festivalRepository.findAll();
    }
    public Optional<Festival> getFestivalById(Long id) {
        return festivalRepository.findById(id);
    }
    public Festival saveFestival(Festival festival) {
        return festivalRepository.save(festival);
    }
    public void deleteFestival(Long id) {
        festivalRepository.deleteById(id);
    }
}

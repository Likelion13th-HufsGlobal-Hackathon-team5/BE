package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.dto.Image.ImageResponseDto;
import com.hackathon_5.Yogiyong_In.dto.Image.ImageUrlPatchRequest;
import com.hackathon_5.Yogiyong_In.service.FestivalImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/calendar/festivals/{festivalId}")
@RequiredArgsConstructor
@Tag(name = "Calendar / Festivals / Images")
public class ImageController {

    private final FestivalImageService festivalImageService;

    // (A) 파일 업로드로 원본 저장 + DB 갱신
    @Operation(summary = "축제 대표 이미지 업로드(원본만 GCS 저장)")
    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDto> updateProfileImage(
            @PathVariable Integer festivalId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String url = festivalImageService.uploadOriginalAndSetProfileUrl(festivalId, file);
        return ResponseEntity.ok(new ImageResponseDto(url));
    }

    // (B) 업로드 없이 URL만 저장하고 싶을 때(선택)
    @Operation(summary = "축제 대표 이미지 URL만 교체(업로드 없음)")
    @PatchMapping(value = "/profile-image-url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageResponseDto> updateProfileImageUrl(
            @PathVariable Integer festivalId,
            @RequestBody @Valid ImageUrlPatchRequest req
    ) {
        String url = festivalImageService.setProfileUrlDirectly(festivalId, req.url());
        return ResponseEntity.ok(new ImageResponseDto(url));
    }
}

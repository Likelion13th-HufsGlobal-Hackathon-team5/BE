package com.hackathon_5.Yogiyong_In.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FestivalImageService {

    private final FestivalRepository festivalRepository;
    private final Storage storage;

    @Value("${gcs.bucket}")
    private String bucket;

    /** 원본만 업로드하고 DB의 imagePath 갱신 */
    @Transactional
    public String uploadOriginalAndSetProfileUrl(Integer festivalId, MultipartFile file) throws IOException {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("Festival not found: " + festivalId));

        validateImage(file);

        String ext = pickExtension(file); // jpg/png/webp/gif 등
        String objectName = "festivals/%d/profile/%s.%s"
                .formatted(festivalId, UUID.randomUUID(), ext);

        BlobId blobId = BlobId.of(bucket, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(safeContentType(file.getContentType(), ext))
                .build();

        storage.create(blobInfo, file.getBytes());

        String publicUrl = "https://storage.googleapis.com/%s/%s".formatted(bucket, objectName);
        festival.setImagePath(publicUrl);
        return publicUrl;
    }

    /** 업로드 없이 URL만 DB에 저장하고 싶을 때(선택 API 용) */
    @Transactional
    public String setProfileUrlDirectly(Integer festivalId, String url) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("Festival not found: " + festivalId));
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("이미지 URL이 비었습니다.");
        }
        festival.setImagePath(url);
        return url;
    }

    // ---- helpers ----
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("빈 파일입니다.");
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/"))
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        // 필요시 용량 제한 (예: 5MB)
        if (file.getSize() > 5_000_000)
            throw new IllegalArgumentException("최대 5MB까지 업로드 가능합니다.");
    }

    private String pickExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        String ct = file.getContentType();
        // 파일명 확장자 우선
        if (name != null && name.contains(".")) {
            String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
            if (!ext.isBlank()) return ext;
        }
        // MIME 기반 fallback
        if (ct != null) {
            if (ct.equals("image/png")) return "png";
            if (ct.equals("image/webp")) return "webp";
            if (ct.equals("image/gif"))  return "gif";
        }
        return "jpg";
    }

    private String safeContentType(String ct, String ext) {
        if (ct != null && ct.startsWith("image/")) return ct;
        return switch (ext) {
            case "png"  -> "image/png";
            case "webp" -> "image/webp";
            case "gif"  -> "image/gif";
            default     -> "image/jpeg";
        };
    }
}

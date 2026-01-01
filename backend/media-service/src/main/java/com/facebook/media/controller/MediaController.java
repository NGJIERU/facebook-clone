package com.facebook.media.controller;

import com.facebook.media.model.Media;
import com.facebook.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Media> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        Media media = mediaService.uploadFile(file, userId);
        return ResponseEntity.ok(media);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> getFileMetadata(@PathVariable String id) {
        Media media = mediaService.getFile(id);
        return ResponseEntity.ok(media);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        mediaService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}

package com.facebook.media.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingService {

    /**
     * Generate multiple size variants of an uploaded image
     * 
     * @param file Original image file
     * @return Map of size names to processed image streams
     */
    public Map<String, InputStream> generateImageVariants(MultipartFile file) throws IOException {
        Map<String, InputStream> variants = new HashMap<>();

        // Original image (stored as-is for high quality viewing)
        variants.put("original", file.getInputStream());

        // Thumbnail (150x150) - for list views, avatars
        variants.put("thumbnail", resizeImage(file, 150, 150, 0.85f));

        // Small (400x400) - for mobile feed
        variants.put("small", resizeImage(file, 400, 400, 0.9f));

        // Medium (800x800) - for desktop feed
        variants.put("medium", resizeImage(file, 800, 800, 0.9f));

        log.info("Generated {} variants for image: {}", variants.size(), file.getOriginalFilename());
        return variants;
    }

    /**
     * Resize image to specified dimensions while maintaining aspect ratio
     */
    private InputStream resizeImage(MultipartFile file, int maxWidth, int maxHeight, float quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(maxWidth, maxHeight)
                .outputQuality(quality)
                .toOutputStream(outputStream);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * Check if file is an image based on content type
     */
    public boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}

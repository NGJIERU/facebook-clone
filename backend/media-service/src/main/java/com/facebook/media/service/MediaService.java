package com.facebook.media.service;

import com.facebook.media.model.Media;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    Media uploadFile(MultipartFile file, Long userId);

    Media getFile(String id);

    void deleteFile(String id);
}

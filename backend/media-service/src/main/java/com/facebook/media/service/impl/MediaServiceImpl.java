package com.facebook.media.service.impl;

import com.facebook.media.model.Media;
import com.facebook.media.repository.MediaRepository;
import com.facebook.media.service.MediaService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MinioClient minioClient;
    private final MediaRepository mediaRepository;

    @Value("${application.minio.bucket}")
    private String bucketName;

    @Value("${application.minio.url}")
    private String minioUrl;

    @Value("${application.minio.public-url}")
    private String publicUrl;

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
                // Set policy to public read
                String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::"
                        + bucketName + "/*\"]}]}";
                minioClient.setBucketPolicy(
                        io.minio.SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
                log.info("Bucket '{}' created successfully with public read policy", bucketName);
            } else {
                log.info("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket", e);
        }
    }

    @Override
    public Media uploadFile(MultipartFile file, Long userId) {
        String originalFileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;

        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build());

            // Construct direct URL using PUBLIC URL
            // Format: http://public-host:9000/bucket-name/filename
            String url = String.format("%s/%s/%s", publicUrl, bucketName, fileName);

            Media media = Media.builder()
                    .userId(userId)
                    .fileName(fileName)
                    .originalFileName(originalFileName)
                    .contentType(contentType)
                    .size(size)
                    .url(url)
                    .build();

            return mediaRepository.save(media);

        } catch (Exception e) {
            log.error("Error uploading file to MinIO", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public Media getFile(String id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
    }

    @Override
    public void deleteFile(String id) {
        Media media = getFile(id);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(media.getFileName())
                            .build());

            mediaRepository.delete(media);

        } catch (Exception e) {
            log.error("Error deleting file from MinIO", e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}

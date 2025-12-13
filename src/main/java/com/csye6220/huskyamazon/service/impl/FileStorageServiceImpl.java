package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地filestoreserviceimplement
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    /**
     * 构造function：Initializefilestorepath
     * @param uploadDir 从 application.properties read的uploaddirectory
     */
    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Could not create the directory where the uploaded files will be stored.",
                    ex
            );
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // 1. Get并清理originalfile名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // 2. generateuniquefile名（prevent同名覆盖）
        String uniqueFileName = generateUniqueFileName(originalFileName);

        try {
            // 3. Checkfile名是否includeillegal字符
            validateFileName(uniqueFileName);

            // 4. 将filesave到目标位置
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 5. returngenerate的file名
            return uniqueFileName;

        } catch (IOException ex) {
            throw new RuntimeException(
                    "Could not store file " + uniqueFileName + ". Please try again!",
                    ex
            );
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }

    @Override
    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }

    /**
     * generateuniquefile名
     * @param originalFileName originalfile名
     * @return UUID + originalfile名
     */
    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "-" + originalFileName;
    }

    /**
     * Validatefile名是否legal
     * @param fileName file名
     * @throws RuntimeException Iffile名includeillegal字符
     */
    private void validateFileName(String fileName) {
        if (fileName.contains("..")) {
            throw new RuntimeException(
                    "Sorry! Filename contains invalid path sequence " + fileName
            );
        }
    }
}
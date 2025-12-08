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
 * 本地文件存储服务实现
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    /**
     * 构造函数：初始化文件存储路径
     * @param uploadDir 从 application.properties 读取的上传目录
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
        // 1. 获取并清理原始文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // 2. 生成唯一文件名（防止同名覆盖）
        String uniqueFileName = generateUniqueFileName(originalFileName);

        try {
            // 3. 检查文件名是否包含非法字符
            validateFileName(uniqueFileName);

            // 4. 将文件保存到目标位置
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 5. 返回生成的文件名
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
     * 生成唯一文件名
     * @param originalFileName 原始文件名
     * @return UUID + 原始文件名
     */
    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "-" + originalFileName;
    }

    /**
     * 验证文件名是否合法
     * @param fileName 文件名
     * @throws RuntimeException 如果文件名包含非法字符
     */
    private void validateFileName(String fileName) {
        if (fileName.contains("..")) {
            throw new RuntimeException(
                    "Sorry! Filename contains invalid path sequence " + fileName
            );
        }
    }
}
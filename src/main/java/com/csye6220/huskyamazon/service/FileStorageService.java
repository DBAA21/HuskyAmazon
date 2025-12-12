package com.csye6220.huskyamazon.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * File storage service interface
 */
public interface FileStorageService {

    /**
     * Store uploaded file
     * @param file Uploaded file
     * @return Unique filename after storage
     * @throws RuntimeException If storage fails
     */
    String storeFile(MultipartFile file);

    /**
     * Delete file (optional, recommended)
     * @param fileName File name to delete
     * @return Whether deletion was successful
     */
    boolean deleteFile(String fileName);

    /**
     * Get file path (optional, recommended)
     * @param fileName File name
     * @return Full path of the file
     */
    Path getFilePath(String fileName);
}

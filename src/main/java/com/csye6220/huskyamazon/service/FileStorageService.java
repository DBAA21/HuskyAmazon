package com.csye6220.huskyamazon.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 存储上传的文件
     * @param file 上传的文件
     * @return 存储后的唯一文件名
     * @throws RuntimeException 如果存储失败
     */
    String storeFile(MultipartFile file);

    /**
     * 删除文件（可选，建议添加）
     * @param fileName 要删除的文件名
     * @return 是否删除成功
     */
    boolean deleteFile(String fileName);

    /**
     * 获取文件路径（可选，建议添加）
     * @param fileName 文件名
     * @return 文件的完整路径
     */
    Path getFilePath(String fileName);
}
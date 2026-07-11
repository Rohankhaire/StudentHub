package com.studenthub.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String subDir);
    Resource loadFileAsResource(String filePath);
    void deleteFile(String filePath);
}

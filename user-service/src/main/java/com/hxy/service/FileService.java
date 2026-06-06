package com.hxy.service;

import com.hxy.utils.JsonData;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    JsonData upload(MultipartFile file);
}

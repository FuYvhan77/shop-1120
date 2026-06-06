package com.hxy.service.impl;

import com.hxy.service.FileService;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.CommonUtil;
import com.hxy.utils.JsonData;
import com.hxy.utils.MinioUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioUtils minioUtils;

    @Override
    public JsonData upload(MultipartFile file) {
        //1，获取文件的原始后缀     123.png
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //2, 生成新的文件名  2026/5/14
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String folder = dtf.format(now);

        //3, 生成唯一文件名
        String fileName = CommonUtil.generateUUID() + suffix;


        //4, 对象全路径
        String objectName =  folder + "/" + fileName;

        try {
            minioUtils.putObject("shop1024", objectName, file.getInputStream(), file.getSize(),file.getContentType());
            String url = minioUtils.getObjectUrl("shop1024", objectName);
            return JsonData.buildSuccess(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
    }
}

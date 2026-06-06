package com.hxy.utils;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class MinioUtils {

    @Autowired
    private MinioClient client;

    /**
     * 创建桶
     */
    public void createBucket(String bucketName) throws Exception {
        boolean exists = client.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            client.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 上传文件
     * @param bucketName 桶名称
     * @param objectName 对象名（可包含路径，如 2024/06/01/uuid.jpg）
     * @param stream 输入流
     * @param fileSize 文件大小
     * @param contentType 媒体类型（如 image/jpeg）
     */
    public void putObject(String bucketName, String objectName,
                          InputStream stream, long fileSize, String contentType) throws Exception {
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(stream, fileSize, -1)  // -1表示分片大小由SDK自动决定
                        .contentType(contentType)
                        .build());
    }

    /**
     * 获取文件外链（带签名，有时效性）
     */
    public String getObjectUrl(String bucketName, String objectName) throws Exception {
        return client.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(60 * 60 * 24)  // 24小时有效
                        .build());
    }

    /**
     * 删除文件
     */
    public void removeObject(String bucketName, String objectName) throws Exception {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }
}
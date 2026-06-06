package com.hxy.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Autowired
    private MinioProp prop;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(prop.getEndpoint())
                .credentials(prop.getAccesskey(), prop.getSecretkwy())
                .build();
    }
}
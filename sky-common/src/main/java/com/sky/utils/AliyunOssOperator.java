package com.sky.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.sky.properties.AliyunOssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

// 将文件上传到阿里云的工具类
@Component
public class AliyunOssOperator {
    @Autowired
    private AliyunOssProperties aliyunOssProperties;

    public String upload(byte[] content, String originalFilename) throws Exception {
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        OSS ossClient = createClient();
        try {
            ossClient.putObject(aliyunOssProperties.getBucketName(), objectName, new ByteArrayInputStream(content));
        } finally {
            ossClient.shutdown();
        }

        return aliyunOssProperties.getEndpoint().split("//")[0] + "//" + aliyunOssProperties.getBucketName() + "." + aliyunOssProperties.getEndpoint().split("//")[1] + "/" + objectName;
    }

    public void deleteByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }

        String objectName = extractObjectName(fileUrl);
        if (objectName.isEmpty()) {
            return;
        }

        OSS ossClient = createClient();
        try {
            ossClient.deleteObject(aliyunOssProperties.getBucketName(), objectName);
        } finally {
            ossClient.shutdown();
        }
    }

    private OSS createClient() {
        try {
            EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
            clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

            return OSSClientBuilder.create()
                    .endpoint(aliyunOssProperties.getEndpoint())
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(clientBuilderConfiguration)
                    .region(aliyunOssProperties.getRegion())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("创建 OSS 客户端失败", e);
        }
    }

    private String extractObjectName(String fileUrl) {
        try {
            URI uri = URI.create(fileUrl);
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                return "";
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            // 兜底: 如果存的是 objectName 而不是 URL, 直接按 objectName 处理
            return fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
        }
    }
}

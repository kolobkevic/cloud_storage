package ru.kolobkevic.cloud_storage.configs;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {
    private final MinioProperties minioProperties;


    @Bean
    public MinioClient getMinioClient() throws Exception {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getUrl(), minioProperties.getPort(), false)
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        createBucketIfNotExists(minioClient);
        return minioClient;
    }

    public void createBucketIfNotExists(MinioClient minioClient) throws Exception{

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
        }
    }
}

package com.layoof.layoof.uploadFile;

import com.layoof.layoof.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@ConditionalOnProperty(name = "layoof.storage.provider", havingValue = "r2")
public class R2FileStorage implements FileStore {

    private final S3Client s3Client;
    private final String bucket;
    private final String baseUrl;

    public R2FileStorage(S3Client s3Client,
                         @Value("${layoof.storage.bucket:}") String bucket,
                         @Value("${layoof.storage.public-url:http://localhost:8080/files}") String publicUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.baseUrl = FileStore.normalizeBaseUrl(publicUrl);
    }

    @Override
    public StoredFile upload(FileUpload file, String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.contentType())
                .contentLength((long) file.size())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.content()));
        } catch (SdkException ex) {
            throw new FileStorageException("Nao foi possivel salvar a imagem no armazenamento", ex);
        }

        return new StoredFile(key, publicUrl(key));
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (SdkException ex) {
            throw new FileStorageException("Nao foi possivel remover a imagem do armazenamento", ex);
        }
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }
}

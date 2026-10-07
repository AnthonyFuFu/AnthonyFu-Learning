package com.anthonyfu.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Service
public class S3Util {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
	private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(10);
	
	@Autowired
	private S3Client s3Client;

	@Autowired
	private S3Presigner s3Presigner;

	// 取得 S3 預簽名下載網址
	public String getPresignedGetObject(String bucketName, String objectKey) {
		GetObjectRequest getObjectRequest = buildGetObjectRequest(bucketName, objectKey);
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(PRESIGNED_URL_DURATION)
				.getObjectRequest(getObjectRequest)
				.build();
		return s3Presigner.presignGetObject(presignRequest).url().toString();
	}

	// 取得 S3 檔案 metadata
	public HeadObjectResponse getStatObject(String bucketName, String objectKey) {
		HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build();
		return s3Client.headObject(request);
	}
	
	public boolean doesObjectExist(String bucketName, String objectKey) {
		try {
			getStatObject(bucketName, objectKey);
			return true;
		} catch (NoSuchKeyException e) {
			return false;
		} catch (S3Exception e) {
			if (e.statusCode() == 404) {
				return false;
			}
			throw e; // 不是找不到，例如權限錯誤，應該直接拋出
		}
	}
	
	// 取得 S3 InputStream
	public ResponseInputStream<GetObjectResponse> getObjectInputStream(String bucketName, String objectKey) {
		GetObjectRequest request = buildGetObjectRequest(bucketName, objectKey);
		return s3Client.getObject(request);
	}

	// 使用 InputStream 上傳檔案，適合大型檔案
	public void putObject(String bucketName, String objectKey, InputStream inputStream, long fileSize, String contentType) {
		PutObjectRequest request = buildPutObjectRequest(bucketName, objectKey, contentType);
		s3Client.putObject(request, RequestBody.fromInputStream(inputStream, fileSize));
	}
	
	public void putObject(String bucketName, String objectKey, InputStream inputStream, long fileSize) {
		putObject(bucketName, objectKey, inputStream, fileSize, null);
	}
	
	// 使用 MultipartFile 上傳檔案
	public void putObject(String bucketName, String objectKey, MultipartFile file) throws IOException {
		try (InputStream inputStream = file.getInputStream()) {
			putObject(bucketName, objectKey, inputStream, file.getSize(), file.getContentType());
		}
	}

	// 使用 byte[] 上傳檔案，適合小型檔案
	public void putObject(String bucketName, String objectKey, byte[] fileBytes, String contentType) {
		PutObjectRequest request = buildPutObjectRequest(bucketName, objectKey, contentType);
		s3Client.putObject(request, RequestBody.fromBytes(fileBytes));
	}

	// 刪除 S3 檔案
	public void deleteObject(String bucketName, String objectKey) {
		try {
			DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build();
			s3Client.deleteObject(request);
			log.info("S3 檔案刪除成功: {}", objectKey);
		} catch (Exception e) {
			log.warn("S3 檔案刪除失敗，若為同檔名覆蓋情境則不影響功能: objectKey={}, message={}", objectKey, e.getMessage());
		}
	}

	// 複製 S3 檔案
	public void copyObject(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
		CopyObjectRequest request = CopyObjectRequest.builder()
				.sourceBucket(sourceBucket)
				.sourceKey(sourceKey)
				.destinationBucket(destinationBucket)
				.destinationKey(destinationKey)
				.build();
		s3Client.copyObject(request);
	}

	// 建立 GetObjectRequest
	private static GetObjectRequest buildGetObjectRequest(String bucketName, String objectKey) {
		return GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();
	}

	// 建立 PutObjectRequest
	private static PutObjectRequest buildPutObjectRequest(String bucketName, String objectKey, String contentType) {
		return PutObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.contentType(normalizeContentType(contentType))
				.build();
	}

	// 避免 contentType 為 null
	private static String normalizeContentType(String contentType) {
		return contentType == null || contentType.isBlank() ? DEFAULT_CONTENT_TYPE : contentType;
	}
	
}

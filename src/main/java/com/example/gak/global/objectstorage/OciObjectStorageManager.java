package com.example.gak.global.objectstorage;

import static com.example.gak.global.apiPayload.code.GeneralErrorCode.*;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gak.global.apiPayload.exception.S3Exception;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class OciObjectStorageManager {

	private static final String IMAGE_TYPE_PREFIX = "image/";
	private static final String STORAGE_PROFILE_PATH = "profiles";
	private static final String STORAGE_SESSION_THUMBNAIL_PATH = "sessionThumbnails";

	@Value("${cloud.oci.bucket-name}")
	private String bucketName;

	@Value("${cloud.oci.namespace}")
	private String namespace;

	@Value("${cloud.oci.region}")
	private String region;

	private final S3Client s3Client;

	public String uploadFile(String keyName, MultipartFile file) {
		validateImage(file);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(keyName)
			.contentType(file.getContentType())
			.build();

		try {
			s3Client.putObject(
				putObjectRequest,
				RequestBody.fromBytes(file.getBytes())
			);
		} catch (IOException e) {
			throw new S3Exception(S3_UPLOAD_FAIL);
		}

		return getPublicUrl(keyName);
	}

	public void deleteFile(String fileUrl) {
		String keyName = extractKeyNameFromUrl(fileUrl);

		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			.bucket(bucketName)
			.key(keyName)
			.build();

		try {
			s3Client.deleteObject(deleteObjectRequest);
		} catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
			throw new S3Exception(S3_DELETE_FAIL);
		}
	}

	public String generateProfileKeyName() {
		String uuid = UUID.randomUUID().toString();
		return STORAGE_PROFILE_PATH + '/' + uuid;
	}

	public String generateSessionThumbnailKeyName() {
		String uuid = UUID.randomUUID().toString();
		return STORAGE_SESSION_THUMBNAIL_PATH + '/' + uuid;
	}

	private void validateImage(MultipartFile image) {
		String contentType = image.getContentType();
		if (contentType == null || !contentType.startsWith(IMAGE_TYPE_PREFIX)) {
			throw new S3Exception(IMAGE_FILE_TYPE_NOT_SUPPORTED);
		}
	}

	private String getPublicUrl(String keyName) {
		return String.format(
			"https://%s.compat.objectstorage.%s.oraclecloud.com/%s/%s",
			namespace,
			region,
			bucketName,
			keyName
		);
	}

	private String extractKeyNameFromUrl(String fileUrl) {
		return URI.create(fileUrl).getPath().substring(1);
	}
}

package com.example.gak.global.aws;

/*@Slf4j
@Component
@RequiredArgsConstructor*/
public class AmazonS3Manager {

	/*private final AmazonS3 amazonS3;
	private final AmazonConfig amazonConfig;

	public String uploadFile(String keyName, MultipartFile file) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		try {
			amazonS3.putObject(
				new PutObjectRequest(amazonConfig.getBucket(), keyName, file.getInputStream(), metadata));
		} catch (Exception e) {
			log.error("S3 upload failed for file: keyName={}", keyName, e);
			throw new S3Exception(S3_UPLOAD_FAIL);
		}

		return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
	}

	public void deleteFile(String fileUrl) {
		try {
			String keyName = URI.create(fileUrl).getPath().substring(1);
			amazonS3.deleteObject(amazonConfig.getBucket(), keyName);
		} catch (Exception e) {
			log.error("S3 delete failed for file: {}", fileUrl, e);
			throw new S3Exception(S3_DELETE_FAIL);
		}
	}

	public String generateProfileKeyName() {
		String uuid = UUID.randomUUID().toString();
		return amazonConfig.getProfilePath() + '/' + uuid;
	}

	public String generateSessionThumbnailKeyName() {
		String uuid = UUID.randomUUID().toString();
		return amazonConfig.getSessionThumbnailPath() + '/' + uuid;
	}*/
}

package com.example.gak.global.configuration;

// 이전 정보 참고용
public class AmazonConfig {

	/*private AWSCredentials awsCredentials;

	@Value("${cloud.aws.credentials.accessKey}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secretKey}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Value("${cloud.aws.s3.path.profile}")
	private String profilePath;

	@Value("${cloud.aws.s3.path.sessionThumbnail}")
	private String sessionThumbnailPath;

	@PostConstruct
	public void init() {
		this.awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
	}

	@Bean
	public AmazonS3 amazonS3() {
		AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		return AmazonS3ClientBuilder.standard()
			.withRegion(region)
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
			.build();
	}

	@Bean
	public AWSCredentialsProvider awsCredentialsProvider() {
		return new AWSStaticCredentialsProvider(awsCredentials);
	}*/
}

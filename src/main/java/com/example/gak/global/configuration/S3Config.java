package com.example.gak.global.configuration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class S3Config {

	@Value("${cloud.oci.namespace}")
	private String namespace;

	@Value("${cloud.oci.region}")
	private String region;

	@Value("${cloud.oci.access-key}")
	private String accessKey;

	@Value("${cloud.oci.secret-key}")
	private String secretKey;

	@Bean
	public S3Client s3Client() {
		String endpoint = String.format(
			"https://%s.compat.objectstorage.%s.oraclecloud.com",
			namespace,
			region
		);
		return S3Client.builder()
			.region(Region.of(region))
			.endpointOverride(URI.create(endpoint))
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)
			))
			.serviceConfiguration(
				S3Configuration.builder()
					.pathStyleAccessEnabled(true)
					.chunkedEncodingEnabled(false)
					.build()
			)
			.build();
	}
}

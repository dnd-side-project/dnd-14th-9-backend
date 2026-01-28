package com.example.gak.domain.member.entity;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.member.entity.enums.SocialProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;

	@Column(nullable = false)
	private String nickname;

	private String profileImageUrl;

	private String bio;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionCategory interestCategory;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SocialProvider socialProvider;

	private String providerId;

	public Member(
		String nickname,
		String profileImageUrl,
		String bio,
		SessionCategory interestCategory,
		SocialProvider socialProvider,
		String providerId
	) {
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.bio = bio;
		this.interestCategory = interestCategory;
		this.socialProvider = socialProvider;
		this.providerId = providerId;
	}
}

package com.example.gak.domain.member.entity;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.common.entity.enums.SessionCategory;

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

	private String email;

	private String bio;

	@Enumerated(EnumType.STRING)
	private SessionCategory firstInterestCategory;

	@Enumerated(EnumType.STRING)
	private SessionCategory secondInterestCategory;

	@Enumerated(EnumType.STRING)
	private SessionCategory thirdInterestCategory;

	@Column(nullable = false)
	private String socialProvider;

	private String providerId;

	private boolean firstLogin;

	private boolean deleted;

	public Member(
		String nickname,
		String profileImageUrl,
		String email,
		String bio,
		SessionCategory firstInterestCategory,
		SessionCategory secondInterestCategory,
		SessionCategory thirdInterestCategory,
		String socialProvider,
		String providerId
	) {
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.email = email;
		this.bio = bio;
		this.firstInterestCategory = firstInterestCategory;
		this.secondInterestCategory = secondInterestCategory;
		this.thirdInterestCategory = thirdInterestCategory;
		this.socialProvider = socialProvider;
		this.providerId = providerId;
		this.firstLogin = true;
		this.deleted = false;
	}

	public String getDisplayNickname() {
		return nickname + " #" + id;
	}

	public void markLoginDone() {
		this.firstLogin = false;
	}

	public void updateProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateEmail(String email) {
		this.email = email;
	}

	public void updateInterestCategories(
		SessionCategory firstInterestCategory,
		SessionCategory secondInterestCategory,
		SessionCategory thirdInterestCategory
	) {
		this.firstInterestCategory = firstInterestCategory;
		this.secondInterestCategory = secondInterestCategory;
		this.thirdInterestCategory = thirdInterestCategory;
	}

	public void deleteProfileImageUrl() {
		this.profileImageUrl = null;
	}

	public void delete() {
		this.deleted = true;
		this.firstLogin = true;
	}

	public void activate() {
		this.deleted = false;
	}
}

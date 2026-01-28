package com.example.gak.domain.sessionroom.entity;

import java.time.LocalDateTime;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.sessionroom.entity.enums.SessionRoomStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionRoom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "session_room_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionCategory category;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String summary;

	@Column(nullable = false)
	private String notice;

	private String thumbnailImageUrl;

	@Column(nullable = false)
	private LocalDateTime startTime;

	@Column(nullable = false)
	private Integer durationMinutes;

	@Column(nullable = false)
	private Integer maxCapacity;

	@Enumerated(EnumType.STRING)
	private SessionRoomStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	public SessionRoom(
		SessionCategory category,
		String title,
		String summary,
		String notice,
		String thumbnailImageUrl,
		LocalDateTime startTime,
		Integer durationMinutes,
		Integer maxCapacity,
		SessionRoomStatus status,
		Member member
	) {
		this.category = category;
		this.title = title;
		this.summary = summary;
		this.notice = notice;
		this.thumbnailImageUrl = thumbnailImageUrl;
		this.startTime = startTime;
		this.durationMinutes = durationMinutes;
		this.maxCapacity = maxCapacity;
		this.status = status;
		this.member = member;
	}
}

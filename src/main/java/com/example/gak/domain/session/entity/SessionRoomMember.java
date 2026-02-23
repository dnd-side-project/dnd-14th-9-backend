package com.example.gak.domain.session.entity;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.session.entity.enums.SessionParticipantRole;
import com.example.gak.domain.session.entity.enums.SessionParticipantStatus;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "session_room_member",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_session_room_member_member_session",
			columnNames = {"member_id", "session_room_id"}
		)
	}
)
public class SessionRoomMember extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "session_room_member_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionParticipantRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionParticipantStatus status = SessionParticipantStatus.FOCUSED;

	private boolean isAbnormalExit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_room_id", nullable = false)
	private SessionRoom sessionRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	private Integer totalFocusSeconds = 0;
	
	private Integer overallSeconds = 0;

	public SessionRoomMember(
		SessionParticipantRole role,
		SessionRoom sessionRoom,
		Member member
	) {
		this.role = role;
		this.isAbnormalExit = false;
		this.sessionRoom = sessionRoom;
		this.member = member;
	}

	public void markAsAbnormalExit() {
		this.isAbnormalExit = true;
	}

	public void toggleStatus() {
		if (this.status == SessionParticipantStatus.FOCUSED) {
			this.status = SessionParticipantStatus.REST;
		} else {
			this.status = SessionParticipantStatus.FOCUSED;
		}
	}

	public void setTotalFocusSeconds(int totalFocusSeconds) {
		this.totalFocusSeconds = totalFocusSeconds;
	}

	public void setOverallSeconds(int overallSeconds) {
		this.overallSeconds = overallSeconds;
	}
}

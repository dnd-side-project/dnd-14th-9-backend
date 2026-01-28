package com.example.gak.domain.sessionroommember.entity;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.sessionroom.entity.SessionRoom;
import com.example.gak.domain.sessionroommember.entity.enums.SessionParticipantRole;

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
public class SessionRoomMember extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "session_room_member_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionParticipantRole role;

	private boolean isAbnormalExit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_room_id", nullable = false)
	private SessionRoom sessionRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

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
}

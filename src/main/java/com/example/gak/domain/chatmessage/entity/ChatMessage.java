package com.example.gak.domain.chatmessage.entity;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.sessionroom.entity.SessionRoom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class ChatMessage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_message_id")
	private Long id;

	@Column(nullable = false)
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_room_id", nullable = false)
	private SessionRoom sessionRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	public ChatMessage(
		String content,
		SessionRoom sessionRoom,
		Member member
	) {
		this.content = content;
		this.sessionRoom = sessionRoom;
		this.member = member;
	}
}

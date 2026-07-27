package com.example.gak.domain.session.entity;

import com.example.gak.domain.common.entity.enums.EmojiType;
import com.example.gak.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	uniqueConstraints = {
		@UniqueConstraint(
			columnNames = {"session_room_id", "member_id", "target_member_id"}
		)
	}
)
public class Reaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reaction_id")
	private Long id;

	private EmojiType emojiType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_member_id", nullable = false)
	private Member targetMember;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_room_id", nullable = false)
	private SessionRoom sessionRoom;

	public void changeEmojiType(EmojiType emojiType) {
		this.emojiType = emojiType;
	}

	public static Reaction create(
		EmojiType emojiType,
		Member member,
		Member targetMember,
		SessionRoom sessionRoom
	) {
		Reaction action = new Reaction();
		action.emojiType = emojiType;
		action.member = member;
		action.targetMember = targetMember;
		action.sessionRoom = sessionRoom;
		return action;
	}
}
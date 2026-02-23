package com.example.gak.domain.session.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gak.domain.common.entity.enums.EmojiType;
import com.example.gak.domain.session.entity.Reaction;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

	Optional<Reaction> findBySessionRoomIdAndMemberIdAndTargetMemberId(
		Long sessionRoomId,
		Long memberId,
		Long targetMemberId
	);

	int countBySessionRoomIdAndEmojiType(Long sessionRoomId, EmojiType emojiType);

	int countBySessionRoomIdAndTargetMemberIdAndEmojiType(
		Long sessionRoomId,
		Long targetMemberId,
		EmojiType emojiType
	);
}

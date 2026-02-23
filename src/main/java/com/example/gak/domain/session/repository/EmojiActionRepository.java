package com.example.gak.domain.session.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gak.domain.session.entity.EmojiAction;

public interface EmojiActionRepository extends JpaRepository<EmojiAction, Long> {

	Optional<EmojiAction> findBySessionRoomIdAndMemberIdAndTargetMemberId(
		Long sessionRoomId,
		Long memberId,
		Long targetMemberId
	);
}

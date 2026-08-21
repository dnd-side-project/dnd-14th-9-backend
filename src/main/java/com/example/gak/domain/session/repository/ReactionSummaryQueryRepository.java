package com.example.gak.domain.session.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gak.domain.session.entity.Reaction;

public interface ReactionSummaryQueryRepository extends JpaRepository<Reaction, Long> {

	@Query("""
		SELECT r.targetMember.id, r.emojiType, COUNT(r)
		FROM Reaction r
		WHERE r.sessionRoom.id = :sessionId
		GROUP BY r.targetMember.id, r.emojiType
		""")
	List<Object[]> countGroupByTargetMemberAndEmojiType(@Param("sessionId") Long sessionId);
}

package com.example.gak.domain.session.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;

public interface SessionRoomRepository extends JpaRepository<SessionRoom, Long>,
	JpaSpecificationExecutor<SessionRoom> {

	void deleteByMemberId(Long memberId);

	boolean existsByMemberIdAndStatusNot(Long memberId, SessionRoomStatus status);

	@EntityGraph(attributePaths = {"member"})
	Optional<SessionRoom> findWithMemberById(Long id);

	@Modifying(clearAutomatically = true)
	@Query("""
		    UPDATE SessionRoom s
		    SET s.currentCount = s.currentCount + 1
		    WHERE s.id = :sessionId
		      AND s.currentCount < s.maxCapacity
		      AND s.status <> com.example.gak.domain.session.entity.enums.SessionRoomStatus.COMPLETED
		""")
	int increaseCountIfAvailable(@Param("sessionId") Long sessionId);

	@Modifying(clearAutomatically = true)
	@Query("""
		    UPDATE SessionRoom s
		    SET s.currentCount = s.currentCount - 1
		    WHERE s.id = :sessionId
		      AND s.currentCount > 0
		      AND s.status <> com.example.gak.domain.session.entity.enums.SessionRoomStatus.COMPLETED
		""")
	int decreaseCount(@Param("sessionId") Long sessionId);
}
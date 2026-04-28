package com.example.gak.domain.session.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gak.domain.member.entity.Member;
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
		      AND (
		        s.status <> com.example.gak.domain.session.entity.enums.SessionRoomStatus.WAITING
		        OR s.member.id = :memberId
		        OR EXISTS (
		          SELECT 1
		          FROM SessionRoomMember srm
		          WHERE srm.sessionRoom = s
		            AND srm.member = s.member
		        )
		        OR s.currentCount < s.maxCapacity - 1
		      )
		""")
	int increaseCountIfJoinable(@Param("sessionId") Long sessionId, @Param("memberId") Long memberId);

	@Modifying(clearAutomatically = true)
	@Query("""
		    UPDATE SessionRoom s
		    SET s.currentCount = s.currentCount - 1
		    WHERE s.id = :sessionId
		      AND s.currentCount > 0
		      AND s.status <> com.example.gak.domain.session.entity.enums.SessionRoomStatus.COMPLETED
		""")
	int decreaseCount(@Param("sessionId") Long sessionId);

	@Modifying(clearAutomatically = true)
	@Query("""
		    UPDATE SessionRoom s
		    SET s.currentCount = s.currentCount - :count
		    WHERE s.id = :sessionRoomId
		      AND s.currentCount >= :count
		      AND s.status = com.example.gak.domain.session.entity.enums.SessionRoomStatus.WAITING
		""")
	int decreaseCountBy(@Param("sessionRoomId") Long sessionRoomId, @Param("count") int count);

	List<SessionRoom> findByStatusAndStartTimeBefore(SessionRoomStatus status, LocalDateTime dateTime);

	List<SessionRoom> findByStatusAndEndTimeBefore(SessionRoomStatus status, LocalDateTime time);

	boolean existsByMemberAndStatusIn(Member member, List<SessionRoomStatus> statuses);
}

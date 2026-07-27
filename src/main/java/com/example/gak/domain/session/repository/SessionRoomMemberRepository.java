package com.example.gak.domain.session.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.SessionRoomMember;
import com.example.gak.domain.session.entity.enums.SessionParticipantRole;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;

public interface SessionRoomMemberRepository extends JpaRepository<SessionRoomMember, Long> {

	void deleteByMemberId(Long memberId);

	boolean existsBySessionRoomIdAndMemberId(Long sessionId, Long memberId);

	@Modifying(clearAutomatically = true)
	@Query("""
		    DELETE FROM SessionRoomMember srm
		    WHERE srm.member.id = :memberId
		      AND srm.sessionRoom.id = :sessionId
		""")
	int deleteByMemberIdAndSessionRoomId(
		@Param("memberId") Long memberId,
		@Param("sessionId") Long sessionId
	);

	List<SessionRoomMember> findBySessionRoom(SessionRoom sessionRoom);

	Optional<SessionRoomMember> findByMemberIdAndSessionRoomId(Long memberId, Long sessionId);

	@Modifying
	@Query("""
		DELETE FROM SessionRoomMember srm
		WHERE srm.member.id IN :memberIds
		AND srm.sessionRoom.id = :sessionRoomId
		""")
	int deleteByMemberIdInAndSessionRoomId(@Param("memberIds") List<Long> memberIds,
		@Param("sessionRoomId") Long sessionRoomId);

	@Query("""
		SELECT srm
		FROM SessionRoomMember srm
		WHERE srm.member.id = :memberId
		AND srm.sessionRoom.status = :status
		""")
	Page<SessionRoomMember> findByMember(
		@Param("memberId") Long memberId,
		@Param("status") SessionRoomStatus status,
		Pageable pageable
	);

	boolean existsBySessionRoomIdAndRole(Long sessionRoomId, SessionParticipantRole role);

	List<SessionRoomMember> findBySessionRoomId(Long sessionRoomId);

	Optional<SessionRoomMember> findFirstBySessionRoomIdOrderByCreatedAtAsc(Long sessionRoomId);
}
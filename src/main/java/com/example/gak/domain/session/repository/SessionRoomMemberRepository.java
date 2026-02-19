package com.example.gak.domain.session.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.SessionRoomMember;

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

	Long member(Member member);
}
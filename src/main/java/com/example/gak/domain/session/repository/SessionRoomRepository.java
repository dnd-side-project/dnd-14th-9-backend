package com.example.gak.domain.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;

public interface SessionRoomRepository extends JpaRepository<SessionRoom, Long>,
	JpaSpecificationExecutor<SessionRoom> {

	void deleteByMemberId(Long memberId);

	boolean existsByMemberIdAndStatusNot(Long memberId, SessionRoomStatus status);
}
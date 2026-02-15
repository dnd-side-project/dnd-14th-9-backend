package com.example.gak.domain.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.gak.domain.session.entity.SessionRoomMember;

public interface SessionRoomMemberRepository extends JpaRepository<SessionRoomMember, Long> {

    void deleteByMemberId(Long memberId);

    boolean existsBySessionRoomIdAndMemberId(Long sessionId, Long memberId);
}
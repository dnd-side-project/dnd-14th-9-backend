package com.example.gak.domain.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.gak.domain.session.entity.SessionRoomMember;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRoomMemberRepository extends JpaRepository<SessionRoomMember, Long> {

    void deleteByMemberId(Long memberId);

    int countBySessionRoomId(Long sessionRoomId);

    boolean existsBySessionRoomIdAndMemberId(Long sessionId, Long memberId);

    @Query("""
                select count(srm) > 0
                from SessionRoomMember srm
                where srm.sessionRoom.id = :sessionId
                  and srm.role = 'HOST'
            """)
    boolean existsSessionHost(@Param("sessionId") Long sessionId);
}
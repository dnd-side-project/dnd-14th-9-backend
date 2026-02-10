package com.example.gak.domain.session.repository;

import com.example.gak.domain.session.entity.SessionRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SessionRoomRepository extends JpaRepository<SessionRoom, Long>,
        JpaSpecificationExecutor<SessionRoom> {

}
package com.example.gak.domain.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gak.domain.task.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByMemberId(Long memberId);

	Optional<Task> findBySessionRoomIdAndMemberId(Long sessionRoomId, Long memberId);

	List<Task> findAllBySessionRoomIdAndMemberIdIn(Long sessionRoomId, List<Long> memberIds);

	@Query("SELECT t FROM Task t JOIN FETCH t.sessionRoom WHERE t.sessionRoom.id = :sessionId AND t.member.id = :memberId")
	Optional<Task> findWithSessionRoomBySessionRoomIdAndMemberId(
		@Param("sessionId") Long sessionId,
		@Param("memberId") Long memberId
	);

	List<Task> findBySessionRoomId(Long sessionRoomId);
}

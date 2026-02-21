package com.example.gak.domain.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gak.domain.task.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByMemberId(Long memberId);

	Optional<Task> findBySessionRoomIdAndMemberId(Long sessionRoomId, Long memberId);

	List<Task> findAllBySessionRoomIdAndMemberIdIn(Long sessionRoomId, List<Long> memberIds);
}

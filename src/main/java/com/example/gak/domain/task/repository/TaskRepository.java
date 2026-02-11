package com.example.gak.domain.task.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gak.domain.task.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByMemberId(Long memberId);
}

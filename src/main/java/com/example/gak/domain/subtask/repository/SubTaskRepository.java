package com.example.gak.domain.subtask.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gak.domain.subtask.entity.SubTask;

public interface SubTaskRepository extends JpaRepository<SubTask, Long> {

	@Modifying
	@Query("""
		DELETE FROM SubTask s WHERE s.task.id IN :taskIds
		""")
	void deleteByTaskIdIn(@Param("taskIds") List<Long> taskIds);

	List<SubTask> findByTaskId(Long taskId);
}

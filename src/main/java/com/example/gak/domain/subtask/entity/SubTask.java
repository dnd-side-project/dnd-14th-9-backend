package com.example.gak.domain.subtask.entity;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.task.entity.Task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubTask extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sub_task_id")
	private Long id;

	@Column(nullable = false)
	private String subTaskTitle;

	private boolean isCompleted;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id", nullable = false)
	public Task task;

	public SubTask(
		String subTaskTitle,
		Task task
	) {
		this.subTaskTitle = subTaskTitle;
		this.isCompleted = false;
		this.task = task;
	}

	public void complete() {
		this.isCompleted = true;
	}
}

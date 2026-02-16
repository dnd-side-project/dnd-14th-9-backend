package com.example.gak.domain.task.entity;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.session.entity.SessionRoom;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
		name = "task",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "task_member_session",
						columnNames = {"member_id", "session_room_id"}
				)
		}
)
public class Task extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "task_id")
	private Long id;

	@Column(nullable = false)
	private String goal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_room_id", nullable = false)
	private SessionRoom sessionRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	public Task(
		String goal,
		SessionRoom sessionRoom,
		Member member
	) {
		this.goal = goal;
		this.sessionRoom = sessionRoom;
		this.member = member;
	}
}

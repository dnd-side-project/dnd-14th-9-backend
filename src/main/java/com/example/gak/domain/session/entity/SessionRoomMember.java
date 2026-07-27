package com.example.gak.domain.session.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.session.entity.enums.SessionParticipantRole;
import com.example.gak.domain.session.entity.enums.SessionParticipantStatus;
import com.example.gak.domain.task.entity.SubTask;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "session_room_member",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_session_room_member_member_session",
			columnNames = {"member_id", "session_room_id"}
		)
	}
)
public class SessionRoomMember extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "session_room_member_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionParticipantRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionParticipantStatus status = SessionParticipantStatus.FOCUSED;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_room_id", nullable = false)
	private SessionRoom sessionRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	private LocalDateTime lastFocusTime = LocalDateTime.now();

	private Integer totalFocusSeconds = 0;

	private Integer overallSeconds = 0;

	private Integer focusRate = 0;

	private Integer achievementRate = 0;

	private Integer heartCount = 0;

	private Integer starCount = 0;

	private Integer thumbsUpCount = 0;

	private Integer thumbsDownCount = 0;

	public SessionRoomMember(
		SessionParticipantRole role,
		SessionRoom sessionRoom,
		Member member
	) {
		this.role = role;
		this.sessionRoom = sessionRoom;
		this.member = member;
	}

	public void changeParticipantRole(SessionParticipantRole role) {
		this.role = role;
	}

	public void toggleStatus() {
		if (this.status == SessionParticipantStatus.FOCUSED) {
			this.status = SessionParticipantStatus.REST;
		} else {
			this.status = SessionParticipantStatus.FOCUSED;
		}
	}

	public void updateAchievementRate(List<SubTask> subTasks) {
		int totalCount = subTasks.size();
		if (totalCount == 0) {
			this.achievementRate = 0;
			return;
		}

		long completedCount = subTasks.stream()
			.filter(SubTask::isCompleted)
			.count();

		this.achievementRate = (int)((double)completedCount / totalCount * 100);
	}

	public void setTotalFocusSeconds(int totalFocusSeconds) {
		this.totalFocusSeconds = totalFocusSeconds;
	}

	public void updateFocusSeconds(int focusSeconds) {
		this.totalFocusSeconds += focusSeconds;
	}

	public void setLastFocusTime(LocalDateTime lastFocusTime) {
		this.lastFocusTime = lastFocusTime;
	}

	public void setOverallSeconds(int overallSeconds) {
		this.overallSeconds = overallSeconds;
	}

	public void updateFocusRate() {
		if (this.overallSeconds <= 0) {
			this.focusRate = 0;
			return;
		}
		this.focusRate = (int)((double)this.totalFocusSeconds / this.overallSeconds * 100);
	}
}

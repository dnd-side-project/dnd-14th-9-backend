package com.example.gak.domain.record.entity;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.member.entity.Member;

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
public class Record extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "record_id")
	private Long id;

	private long participationTime;

	private long focusedTime;

	private int completedSessionCount;

	private int totalTodoCount;

	private int achievedTodoCount;

	private int devSessionCount;

	private int designSessionCount;

	private int planningPmSessionCount;

	private int careerSelfDevSessionCount;

	private int studyReadingSessionCount;

	private int creativeSessionCount;

	private int teamProjectSessionCount;

	private int etcSessionCount;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, unique = true)
	private Member member;

	public Record(
		long participationTime,
		long focusedTime,
		int completedSessionCount,
		int totalTodoCount,
		int achievedTodoCount,
		int devSessionCount,
		int designSessionCount,
		int planningPmSessionCount,
		int careerSelfDevSessionCount,
		int studyReadingSessionCount,
		int creativeSessionCount,
		int teamProjectSessionCount,
		int etcSessionCount,
		Member member
	) {
		this.participationTime = participationTime;
		this.focusedTime = focusedTime;
		this.completedSessionCount = completedSessionCount;
		this.totalTodoCount = totalTodoCount;
		this.achievedTodoCount = achievedTodoCount;
		this.devSessionCount = devSessionCount;
		this.designSessionCount = designSessionCount;
		this.planningPmSessionCount = planningPmSessionCount;
		this.careerSelfDevSessionCount = careerSelfDevSessionCount;
		this.studyReadingSessionCount = studyReadingSessionCount;
		this.creativeSessionCount = creativeSessionCount;
		this.teamProjectSessionCount = teamProjectSessionCount;
		this.etcSessionCount = etcSessionCount;
		this.member = member;
	}

	public void increaseParticipationTime(long seconds) {
		this.participationTime += seconds;
	}

	public void increaseFocusedTime(long seconds) {
		this.focusedTime += seconds;
	}

	public void increaseCompletedSessionCount() {
		this.completedSessionCount++;
	}

	public void increaseTotalTodoCount(int count) {
		this.totalTodoCount += count;
	}

	public void increaseAchievedTodoCount(int count) {
		this.achievedTodoCount += count;
	}

	public void increaseSessionCategoryCount(SessionCategory category) {
		switch (category) {
			case DEVELOPMENT -> devSessionCount++;
			case DESIGN -> designSessionCount++;
			case PLANNING_PM -> planningPmSessionCount++;
			case CAREER_SELF_DEVELOPMENT -> careerSelfDevSessionCount++;
			case STUDY_READING -> studyReadingSessionCount++;
			case CREATIVE -> creativeSessionCount++;
			case TEAM_PROJECT -> teamProjectSessionCount++;
			case FREE -> etcSessionCount++;
		}
	}
}

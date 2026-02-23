package com.example.gak.domain.record.entity;

import java.util.EnumMap;
import java.util.Map;

import com.example.gak.domain.common.entity.BaseEntity;
import com.example.gak.domain.common.entity.enums.EmojiType;
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

	private long totalParticipationTime;

	private long focusedTime;

	private int participationSessionCount;

	private int totalTodoCount;

	private int completedTodoCount;

	private int devSessionCount;

	private int designSessionCount;

	private int planningPmSessionCount;

	private int careerSelfDevSessionCount;

	private int studyReadingSessionCount;

	private int creativeSessionCount;

	private int teamProjectSessionCount;

	private int etcSessionCount;

	private int totalEmojiCount;

	private int heartEmojiCount;

	private int thumbsUpEmojiCount;

	private int thumbsDownEmojiCount;

	private int starEmojiCount;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, unique = true)
	private Member member;

	public Record(Member member) {
		this.totalParticipationTime = 0;
		this.focusedTime = 0;
		this.participationSessionCount = 0;
		this.totalTodoCount = 0;
		this.completedTodoCount = 0;
		this.devSessionCount = 0;
		this.designSessionCount = 0;
		this.planningPmSessionCount = 0;
		this.careerSelfDevSessionCount = 0;
		this.studyReadingSessionCount = 0;
		this.creativeSessionCount = 0;
		this.teamProjectSessionCount = 0;
		this.etcSessionCount = 0;
		this.totalEmojiCount = 0;
		this.heartEmojiCount = 0;
		this.thumbsUpEmojiCount = 0;
		this.thumbsDownEmojiCount = 0;
		this.starEmojiCount = 0;
		this.member = member;
	}

	/* =========================
	 * 상태 변경 메서드 - 카운트 증가
	 * ========================= */

	public void increaseParticipationTime(long seconds) {
		this.totalParticipationTime += seconds;
	}

	public void increaseFocusedTime(long seconds) {
		this.focusedTime += seconds;
	}

	public void increaseTotalTodoCount(int count) {
		this.totalTodoCount += count;
	}

	public void increaseCompletedTodoCount(int count) {
		this.completedTodoCount += count;
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
		participationSessionCount++;
	}

	public void increaseEmojiTypesCount(Map<EmojiType, Integer> emojiTypes) {
		emojiTypes.forEach((type, count) -> {
			switch (type) {
				case HEART -> heartEmojiCount += count;
				case THUMBS_UP -> thumbsUpEmojiCount += count;
				case THUMBS_DOWN -> thumbsDownEmojiCount += count;
				case STAR -> starEmojiCount += count;
			}
			totalEmojiCount += count;
		});
	}

	/* =========================
	 * 비율 계산 메서드
	 * ========================= */

	public int getFocusRate() {
		if (totalParticipationTime == 0) {
			return 0;
		}

		return (int)Math.round((focusedTime * 100.0) / totalParticipationTime);
	}

	public int getTodoCompletionRate() {
		if (totalTodoCount == 0) {
			return 0;
		}

		return (int)Math.round((completedTodoCount * 100.0) / totalTodoCount);
	}

	public int getCategoryParticipationRate(SessionCategory category) {
		if (participationSessionCount == 0) {
			return 0;
		}

		int count = getCategoryCount(category);

		return (int)Math.round((count * 100.0 / participationSessionCount));
	}

	/* =========================
	 * 통계 조회 - 카테고리, 이모지
	 * ========================= */

	public Map<SessionCategory, Integer> getCategoryCounts() {
		Map<SessionCategory, Integer> map = new EnumMap<>(SessionCategory.class);

		map.put(SessionCategory.DEVELOPMENT, devSessionCount);
		map.put(SessionCategory.DESIGN, designSessionCount);
		map.put(SessionCategory.PLANNING_PM, planningPmSessionCount);
		map.put(SessionCategory.CAREER_SELF_DEVELOPMENT, careerSelfDevSessionCount);
		map.put(SessionCategory.STUDY_READING, studyReadingSessionCount);
		map.put(SessionCategory.CREATIVE, creativeSessionCount);
		map.put(SessionCategory.TEAM_PROJECT, teamProjectSessionCount);
		map.put(SessionCategory.FREE, etcSessionCount);

		return map;
	}

	public Map<EmojiType, Integer> getEmojiTypeCounts() {
		Map<EmojiType, Integer> map = new EnumMap<>(EmojiType.class);

		map.put(EmojiType.HEART, heartEmojiCount);
		map.put(EmojiType.THUMBS_UP, thumbsUpEmojiCount);
		map.put(EmojiType.THUMBS_DOWN, thumbsDownEmojiCount);
		map.put(EmojiType.STAR, starEmojiCount);

		return map;
	}

	private int getCategoryCount(SessionCategory category) {
		return switch (category) {
			case DEVELOPMENT -> devSessionCount;
			case DESIGN -> designSessionCount;
			case PLANNING_PM -> planningPmSessionCount;
			case CAREER_SELF_DEVELOPMENT -> careerSelfDevSessionCount;
			case STUDY_READING -> studyReadingSessionCount;
			case CREATIVE -> creativeSessionCount;
			case TEAM_PROJECT -> teamProjectSessionCount;
			case FREE -> etcSessionCount;
		};
	}
}

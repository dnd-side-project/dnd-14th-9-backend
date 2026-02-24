package com.example.gak.domain.member.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gak.domain.common.entity.enums.EmojiType;
import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.member.converter.MemberConverter;
import com.example.gak.domain.member.dto.MemberResponseDTO;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.member.repository.MemberRepository;
import com.example.gak.domain.record.entity.Record;
import com.example.gak.domain.record.repository.RecordRepository;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;
import com.example.gak.domain.session.repository.SessionRoomMemberRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService {

	private final MemberRepository memberRepository;
	private final RecordRepository recordRepository;
	private final SessionRoomMemberRepository sessionRoomMemberRepository;

	public MemberResponseDTO.GetProfileResponseDTO getProfile(Long memberId) {
		Member member = findMember(memberId);
		Record record = recordRepository.findByMemberId(memberId);

		return MemberConverter.toGetProfileResponseDTO(member, record);
	}

	public MemberResponseDTO.GetMemberResponseDTO getMember(Long memberId) {
		Member member = findMember(memberId);

		return MemberConverter.toGetMemberResponseDTO(member);
	}

	public MemberResponseDTO.GetReportStats getReportStats(Long memberId) {
		Record record = recordRepository.findByMemberId(memberId);

		Map<SessionCategory, Integer> categoryCounts = record.getCategoryCounts();
		List<MemberResponseDTO.SessionParticipationStat> sessionParticipationStats =
			categoryCounts.entrySet().stream()
				.map(entry -> MemberResponseDTO.SessionParticipationStat.builder()
					.categoryName(entry.getKey())
					.count(entry.getValue())
					.rate(record.getCategoryParticipationRate(entry.getKey()))
					.build()
				)
				.sorted(
					Comparator.comparing(MemberResponseDTO.SessionParticipationStat::getCount)
						.reversed()
						.thenComparing(stat -> stat.getCategoryName().name())
				)
				.limit(4)
				.toList();

		Map<EmojiType, Integer> emojiTypeCounts = record.getEmojiTypeCounts();
		List<MemberResponseDTO.ReceivedEmojiStat> receivedEmojiStats =
			emojiTypeCounts.entrySet().stream()
				.map(entry -> MemberResponseDTO.ReceivedEmojiStat.builder()
					.emojiName(entry.getKey())
					.count(entry.getValue())
					.build()
				)
				.sorted(
					Comparator.comparing(MemberResponseDTO.ReceivedEmojiStat::getCount)
						.reversed()
						.thenComparing(stat -> stat.getEmojiName().name())
				)
				.toList();

		return MemberResponseDTO.GetReportStats.builder()
			.focusedTime(record.getFocusedTime())
			.totalParticipationTime(record.getTotalParticipationTime())
			.todoCompletionRate(record.getTodoCompletionRate())
			.focusRate(record.getFocusRate())
			.sessionParticipationStats(sessionParticipationStats)
			.receivedEmojis(receivedEmojiStats)
			.build();
	}

	public MemberResponseDTO.GetReportSessionsResponseDTO getReportSessions(Long memberId, Pageable pageable) {
		Page<MemberResponseDTO.GetReportSessionResponseDTO> page = sessionRoomMemberRepository.findByMember(
			memberId,
			SessionRoomStatus.COMPLETED,
			pageable
		).map(srm -> {
			SessionRoom session = srm.getSessionRoom();

			return MemberResponseDTO.GetReportSessionResponseDTO.builder()
				.title(session.getTitle())
				.category(session.getCategory())
				.currentCount(session.getCurrentCount())
				.maxCapacity(session.getMaxCapacity())
				.durationTime(srm.getOverallSeconds())
				.startTime(session.getStartTime())
				.focusedTime(srm.getTotalFocusSeconds())
				.focusRate(srm.getFocusRate())
				.todoCompletionRate(srm.getAchievementRate())
				.build();
		});

		return MemberResponseDTO.GetReportSessionsResponseDTO.builder()
			.listSize(page.getSize())
			.totalPage(page.getTotalPages())
			.totalElements(page.getTotalElements())
			.isFirst(page.isFirst())
			.isLast(page.isLast())
			.sessions(page.getContent())
			.build();
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));
	}
}

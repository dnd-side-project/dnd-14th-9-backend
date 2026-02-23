package com.example.gak.domain.member.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService {

	private final MemberRepository memberRepository;
	private final RecordRepository recordRepository;

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

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));
	}
}

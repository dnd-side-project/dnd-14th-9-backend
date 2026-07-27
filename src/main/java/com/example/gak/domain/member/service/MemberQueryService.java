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

	public MemberResponseDTO.GetReportStatsResponseDTO getReportStats(Long memberId) {
		Record record = recordRepository.findByMemberId(memberId);

		Map<SessionCategory, Integer> categoryCounts = record.getCategoryCounts();
		List<MemberResponseDTO.SessionParticipationStatResponseDTO> sessionParticipationStats =
			categoryCounts.entrySet().stream()
				.map(entry -> MemberResponseDTO.SessionParticipationStatResponseDTO.builder()
					.categoryName(entry.getKey())
					.count(entry.getValue())
					.rate(record.getCategoryParticipationRate(entry.getKey()))
					.build()
				)
				.sorted(
					Comparator.comparing(MemberResponseDTO.SessionParticipationStatResponseDTO::getCount)
						.reversed()
						.thenComparing(stat -> stat.getCategoryName().name())
				)
				.limit(4)
				.toList();

		Map<EmojiType, Integer> emojiTypeCounts = record.getEmojiTypeCounts();
		List<MemberResponseDTO.ReceivedEmojiStatResponseDTO> receivedEmojiStats =
			emojiTypeCounts.entrySet().stream()
				.map(entry -> MemberResponseDTO.ReceivedEmojiStatResponseDTO.builder()
					.emojiName(entry.getKey())
					.count(entry.getValue())
					.build()
				)
				.sorted(
					Comparator.comparing(MemberResponseDTO.ReceivedEmojiStatResponseDTO::getCount)
						.reversed()
						.thenComparing(stat -> stat.getEmojiName().name())
				)
				.toList();

		return MemberResponseDTO.GetReportStatsResponseDTO.of(
			record,
			sessionParticipationStats,
			receivedEmojiStats
		);
	}

	public MemberResponseDTO.GetReportSessionsResponseDTO getReportSessions(Long memberId, Pageable pageable) {
		Page<MemberResponseDTO.GetReportSessionResponseDTO> page = sessionRoomMemberRepository.findByMember(
			memberId,
			SessionRoomStatus.COMPLETED,
			pageable
		).map(MemberResponseDTO.GetReportSessionResponseDTO::from);

		return MemberResponseDTO.GetReportSessionsResponseDTO.from(page);
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));
	}
}

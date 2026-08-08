package com.example.gak.domain.session.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gak.domain.common.entity.enums.EmojiType;
import com.example.gak.domain.session.converter.SessionConverter;
import com.example.gak.domain.session.dto.ReactionSummaryResponseDTO;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.SessionRoomMember;
import com.example.gak.domain.session.repository.ReactionSummaryQueryRepository;
import com.example.gak.domain.session.repository.SessionRoomMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReactionSummaryQueryService {

	private static final int EMOJI_TYPE_COUNT = 4;
	private static final SessionResponseDTO.EmojiResultResponseDTO ZERO =
		SessionConverter.toSumEmojiResultResponseDTO(0, 0, 0, 0);

	private final SessionQueryService sessionQueryService;
	private final ReactionSummaryQueryRepository reactionSummaryQueryRepository;
	private final SessionRoomMemberRepository sessionRoomMemberRepository;

	public record ReactionSnapshot(
		SessionResponseDTO.EmojiResultResponseDTO total,
		Map<Long, SessionResponseDTO.EmojiResultResponseDTO> byMember
	) {
	}

	public ReactionSnapshot getReactionSnapshot(Long sessionId) {
		SessionResponseDTO.EmojiResultResponseDTO total = sessionQueryService.getReactionStatus(sessionId);
		return new ReactionSnapshot(total, buildByMemberSummary(sessionId));
	}

	public ReactionSummaryResponseDTO getReactionSummaryForMember(Long sessionId, Long memberId) {
		return toPersonalized(getReactionSnapshot(sessionId), memberId);
	}

	public ReactionSummaryResponseDTO toPersonalized(ReactionSnapshot snapshot, Long memberId) {
		return ReactionSummaryResponseDTO.builder()
			.total(snapshot.total())
			.my(snapshot.byMember().getOrDefault(memberId, ZERO))
			.build();
	}

	private Map<Long, SessionResponseDTO.EmojiResultResponseDTO> buildByMemberSummary(Long sessionId) {
		Map<Long, int[]> countsByMember = new HashMap<>();
		for (SessionRoomMember roomMember : sessionRoomMemberRepository.findBySessionRoomId(sessionId)) {
			countsByMember.computeIfAbsent(roomMember.getMember().getId(), k -> new int[EMOJI_TYPE_COUNT]);
		}

		for (Object[] row : reactionSummaryQueryRepository.countGroupByTargetMemberAndEmojiType(sessionId)) {
			Long memberId = (Long)row[0];
			EmojiType emojiType = (EmojiType)row[1];
			long count = (Long)row[2];

			int[] counts = countsByMember.computeIfAbsent(memberId, k -> new int[EMOJI_TYPE_COUNT]);
			counts[emojiTypeIndex(emojiType)] += (int)count;
		}

		Map<Long, SessionResponseDTO.EmojiResultResponseDTO> byMember = new HashMap<>();
		for (Map.Entry<Long, int[]> entry : countsByMember.entrySet()) {
			int[] c = entry.getValue();
			byMember.put(entry.getKey(), SessionConverter.toSumEmojiResultResponseDTO(c[0], c[1], c[2], c[3]));
		}
		return byMember;
	}

	private int emojiTypeIndex(EmojiType emojiType) {
		return switch (emojiType) {
			case HEART -> 0;
			case STAR -> 1;
			case THUMBS_UP -> 2;
			case THUMBS_DOWN -> 3;
		};
	}
}

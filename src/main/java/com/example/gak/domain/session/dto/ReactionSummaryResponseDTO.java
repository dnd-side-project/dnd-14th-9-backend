package com.example.gak.domain.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "세션 리액션 요약 응답 (본인 것만)")
@Builder
@Getter
public class ReactionSummaryResponseDTO {
	private SessionResponseDTO.EmojiResultResponseDTO total;
	private SessionResponseDTO.EmojiResultResponseDTO my;
}

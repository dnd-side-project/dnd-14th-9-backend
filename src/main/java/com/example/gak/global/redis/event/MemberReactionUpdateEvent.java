package com.example.gak.global.redis.event;

public record MemberReactionUpdateEvent(Long sessionId, Long memberId) {
}

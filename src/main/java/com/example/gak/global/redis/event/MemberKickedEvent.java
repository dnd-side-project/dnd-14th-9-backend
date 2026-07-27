package com.example.gak.global.redis.event;

import java.util.List;

public record MemberKickedEvent(Long sessionId, List<Long> memberIds) {
}

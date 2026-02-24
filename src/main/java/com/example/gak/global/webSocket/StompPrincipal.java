package com.example.gak.global.webSocket;

import java.security.Principal;

public class StompPrincipal implements Principal {

	private final Long memberId;

	public StompPrincipal(Long memberId) {
		this.memberId = memberId;
	}

	@Override
	public String getName() {
		return memberId.toString();
	}

	public Long getMemberId() {
		return memberId;
	}
}

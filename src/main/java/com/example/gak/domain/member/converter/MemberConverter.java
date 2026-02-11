package com.example.gak.domain.member.converter;

import com.example.gak.domain.member.dto.MemberResponseDTO;
import com.example.gak.domain.member.entity.Member;

public class MemberConverter {

	public static MemberResponseDTO.GetMemberResponseDTO toGetMemberResponseDTO(Member member) {
		return MemberResponseDTO.GetMemberResponseDTO.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getProfileImageUrl())
			.bio(member.getBio())
			.firstInterestCategory(member.getFirstInterestCategory())
			.secondInterestCategory(member.getSecondInterestCategory())
			.thirdInterestCategory(member.getThirdInterestCategory())
			.firstLogin(member.isFirstLogin())
			.build();
	}
}

package com.example.gak.domain.member.converter;

import com.example.gak.domain.member.dto.MemberResponseDTO;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.record.entity.Record;

public class MemberConverter {

	public static MemberResponseDTO.GetProfileResponseDTO toGetProfileResponseDTO(Member member, Record record) {
		return MemberResponseDTO.GetProfileResponseDTO.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getProfileImageUrl())
			.email(member.getEmail())
			.socialProvider(member.getSocialProvider())
			.totalParticipationTime(record.getTotalParticipationMinutes())
			.focusedTime(record.getFocusedMinutes())
			.focusRate(record.getFocusRate())
			.totalTodoCount(record.getTotalTodoCount())
			.completedTodoCount(record.getCompletedTodoCount())
			.todoCompletionRate(record.getTodoCompletionRate())
			.firstLogin(member.isFirstLogin())
			.build();
	}

	public static MemberResponseDTO.GetMemberResponseDTO toGetMemberResponseDTO(Member member) {
		return MemberResponseDTO.GetMemberResponseDTO.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getProfileImageUrl())
			.email(member.getEmail())
			.bio(member.getBio())
			.firstInterestCategory(member.getFirstInterestCategory())
			.secondInterestCategory(member.getSecondInterestCategory())
			.thirdInterestCategory(member.getThirdInterestCategory())
			.build();
	}

	public static MemberResponseDTO.UpdateMemberResponseDTO toUpdateMemberResponseDTO(Member member) {
		return MemberResponseDTO.UpdateMemberResponseDTO.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.email(member.getEmail())
			.profileImageUrl(member.getProfileImageUrl())
			.bio(member.getBio())
			.firstInterestCategory(member.getFirstInterestCategory())
			.secondInterestCategory(member.getSecondInterestCategory())
			.thirdInterestCategory(member.getThirdInterestCategory())
			.build();
	}
}

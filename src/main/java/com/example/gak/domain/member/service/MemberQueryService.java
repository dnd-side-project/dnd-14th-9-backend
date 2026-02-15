package com.example.gak.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));

		Record record = recordRepository.findByMemberId(memberId);

		return MemberConverter.toGetProfileResponseDTO(member, record);
	}

	public MemberResponseDTO.GetMemberResponseDTO getMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));

		return MemberConverter.toGetMemberResponseDTO(member);
	}
}

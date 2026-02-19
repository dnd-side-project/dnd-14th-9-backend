package com.example.gak.domain.record.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.record.entity.Record;

public interface RecordRepository extends JpaRepository<Record, Long> {

	void deleteByMemberId(Long memberId);

	Record findByMemberId(Long memberId);

	Record findByMember(Member member);
}

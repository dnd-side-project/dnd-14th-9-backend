package com.example.gak.domain.record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.record.entity.Record;

public interface RecordRepository extends JpaRepository<Record, Long> {

	void deleteByMemberId(Long memberId);

	Record findByMemberId(Long memberId);

	Record findByMember(Member member);

	@Query("SELECT r FROM Record r WHERE r.member.id IN :memberIds")
	List<Record> findByMemberIdIn(@Param("memberIds") List<Long> memberIds);
}

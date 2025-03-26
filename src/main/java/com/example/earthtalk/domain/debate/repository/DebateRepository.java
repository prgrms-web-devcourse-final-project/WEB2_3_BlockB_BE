package com.example.earthtalk.domain.debate.repository;

import com.example.earthtalk.domain.debate.entity.RoomType;
import java.util.Optional;
import java.util.UUID;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.earthtalk.domain.debate.entity.Debate;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface DebateRepository extends JpaRepository<Debate, Long> {

	@Lock(LockModeType.PESSIMISTIC_READ)
	@Query("select d from debates d left join fetch d.news where d.uuid = :uuid")
	Optional<Debate> findByUuid(UUID uuid);

	@Query("SELECT d FROM debates d "+
			"WHERE (:query IS NULL OR d.title LIKE CONCAT('%', :query, '%')) AND " +
			"(:continent IS NULL OR d.continent = :continent) AND " +
			"(:category IS NULL OR d.category = :category) AND " +
			"(:member IS NULL OR d.member = :member) AND " +
			"d.status = 'closed'" +
			"ORDER BY CASE " +
			"WHEN :sort = 'popular' THEN (d.agreeNumber + d.disagreeNumber + d.neutralNumber)" +
			"ELSE d.updatedAt " +
			"END DESC")
	Page<Debate> findFinishDebatesByParams(@Param("query") String query,
										   @Param("continent")ContinentType continent,
										   @Param("category") CategoryType category,
										   @Param("member") MemberNumberType member,
										   @Param("sort") String sort,
										   Pageable pageable);

	@Query("update debates d set d.status = :status where d.uuid = :uuid")
	@Modifying
	void updateStatusByUuid(RoomType status, UUID uuid);
}
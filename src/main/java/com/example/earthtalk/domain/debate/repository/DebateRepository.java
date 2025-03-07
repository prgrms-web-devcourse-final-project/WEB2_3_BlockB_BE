package com.example.earthtalk.domain.debate.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.earthtalk.domain.debate.entity.Debate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DebateRepository extends JpaRepository<Debate, Long> {

	Optional<Debate> findByUuid(UUID uuid);

	@Query("SELECT d FROM debates d "+
			"WHERE (:query IS NULL OR d.title LIKE CONCAT('%', :query, '%')) AND " +
			"(:continent IS NULL OR d.continent = :continent) AND " +
			"(:category IS NULL OR d.category = :category) AND " +
			"(:time IS NULL OR d.time = :time) AND " +
			"d.status = 0" +
			"ORDER BY CASE " +
			"WHEN :sort = 'popular' THEN (d.agreeNumber + d.disagreeNumber + d.neutralNumber)" +
			"ELSE d.updatedAt " +
			"END DESC")
	Page<Debate> findFinishDebatesByParams(@Param("query") String query,
										   @Param("continent")ContinentType continent,
										   @Param("category") CategoryType category,
										   @Param("time") TimeType time,
										   @Param("sort") String sort,
										   Pageable pageable);
}
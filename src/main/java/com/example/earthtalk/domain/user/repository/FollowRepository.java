package com.example.earthtalk.domain.user.repository;

import com.example.earthtalk.domain.user.entity.Follow;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM follows f WHERE f.followee.id = :followeeId AND f.follower.id = :followerId")
    void deleteByFollows(@Param("followeeId") long followeeId, @Param("followerId") long followerId);
}

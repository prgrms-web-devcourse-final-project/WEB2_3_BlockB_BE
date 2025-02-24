package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Modifying
    @Query("DELETE FROM likes l WHERE l.user.id=:userId AND l.news.id=:newsId")
    void deleteByUserIdAndNewsId(Long userId, Long newsId);
}

package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.entity.Like;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {

    @Modifying
    @Transactional
    @Query("DELETE FROM likes l WHERE l.user.id=:userId AND l.news.id=:newsId")
    void deleteByUserIdAndNewsId(@Param("userId") Long userId, @Param("newsId") Long newsId);

}

package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.entity.Bookmark;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkRepositoryCustom {

    @Modifying
    @Transactional
    @Query("DELETE FROM bookmarks b WHERE b.user.id=:userId AND b.news.id=:newsId")
    void deleteByUserIdAndNewsId(@Param("userId") Long userId, @Param("newsId") Long newsId);
}

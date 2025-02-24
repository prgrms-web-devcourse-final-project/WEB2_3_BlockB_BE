package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Modifying
    @Query("DELETE FROM bookmarks b WHERE b.user.id=:userId AND b.news.id=:newsId")
    void deleteByUserIdAndNewsId(Long userId, Long newsId);
}

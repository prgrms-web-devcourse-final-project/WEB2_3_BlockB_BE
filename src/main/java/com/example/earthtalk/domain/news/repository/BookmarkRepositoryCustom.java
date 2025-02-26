package com.example.earthtalk.domain.news.repository;

public interface BookmarkRepositoryCustom {

    boolean existsByUserIdAndNewsId(Long userId, Long newsId);
}

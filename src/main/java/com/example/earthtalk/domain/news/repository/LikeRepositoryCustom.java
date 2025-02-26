package com.example.earthtalk.domain.news.repository;

public interface LikeRepositoryCustom {
    boolean existsByUserIdAndNewsId(Long userId, Long newsId);
}

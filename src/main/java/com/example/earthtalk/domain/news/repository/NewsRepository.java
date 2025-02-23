package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.NewsType;
import com.example.earthtalk.global.constant.ContinentType;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    @Query("SELECT MAX(n.deliveryTime) FROM news n WHERE n.newsType=:newsType AND n.continent = :continent ")
    LocalDateTime latestNews(@Param("newsType") NewsType newsType, @Param("continent") ContinentType continent);
    @Query("SELECT COUNT(l) FROM likes l WHERE l.news.id=:newsId")
    Long countNewsLike(Long newsId);
    @Query("SELECT n.deliveryTime FROM news n WHERE n.id=:newsId")
    LocalDateTime getNewsDeliveryTime(Long newsId);
}

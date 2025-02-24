package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.dto.NewsListDTO;
import com.example.earthtalk.domain.news.entity.Bookmark;
import com.example.earthtalk.domain.news.entity.Like;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.global.constant.ContinentType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface NewsFilterRepository {

    Slice<NewsListDTO> filterNews(String continent, String query, String sort, Long newsId);

    List<NewsListDTO> newsRanking();

    boolean isLiked(Long userId, Long newsId);

    boolean isMarked(Long userId, Long newsId);
}

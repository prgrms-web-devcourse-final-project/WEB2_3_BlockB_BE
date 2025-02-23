package com.example.earthtalk.domain.news.service;

import com.example.earthtalk.domain.news.dto.NewsListDTO;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.repository.NewsFilterRepository;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsDataService {

    private final NewsFilterRepository newsFilterRepository;

    public Slice<NewsListDTO> getNewsByFilter(String continent, String query, String sort, Long newsId) {
        return newsFilterRepository.filterNews( continent, query, sort, newsId);
    }
}

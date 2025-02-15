package com.example.earthtalk.domain.news.service;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    public List<News> getAllNews() {
        return newsRepository.findAll();
    }
}

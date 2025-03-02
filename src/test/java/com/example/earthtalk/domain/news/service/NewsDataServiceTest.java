package com.example.earthtalk.domain.news.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.earthtalk.domain.news.dto.response.NewsDetailResponse;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.repository.NewsFilterRepository;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.global.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NewsDataServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsFilterRepository newsFilterRepository;

    @InjectMocks
    private NewsDataService newsDataService;

    private final Long newsId = 1L;
    private final Long userId = 10L;
    private final String newsLink = "https://example.com/news/1";


    @Test
    void newsDetailWithUserId() {
        // given
        News news = News.builder().id(newsId).link(newsLink).build();

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(news));
        when(newsRepository.countNewsLike(newsId)).thenReturn(5L);
        when(newsRepository.countNewsBookmark(newsId)).thenReturn(3L);
        when(newsFilterRepository.isLiked(userId, newsId)).thenReturn(true);
        when(newsFilterRepository.isMarked(userId, newsId)).thenReturn(false);

        // when
        NewsDetailResponse response = newsDataService.getNewsDetail(newsId, userId);

        // then
        assertThat(response.like()).isEqualTo(5L);
        assertThat(response.mark()).isEqualTo(3L);
        assertThat(response.link()).isEqualTo(newsLink);
        assertThat(response.liked()).isTrue();
        assertThat(response.marked()).isFalse();
    }

    @Test
    void newsDetailWithoutUserId() {
        // given
        News news = News.builder().id(newsId).link(newsLink).build();

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(news));
        when(newsRepository.countNewsLike(newsId)).thenReturn(5L);
        when(newsRepository.countNewsBookmark(newsId)).thenReturn(3L);

        // when
        NewsDetailResponse response = newsDataService.getNewsDetail(newsId, null);

        // then
        assertThat(response.like()).isEqualTo(5L);
        assertThat(response.mark()).isEqualTo(3L);
        assertThat(response.link()).isEqualTo(newsLink);
        assertThat(response.liked()).isFalse();
        assertThat(response.marked()).isFalse();
    }

    @Test
    void newsDoNotExist() {
        when(newsRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> newsDataService.getNewsDetail(2L, userId));
    }



}

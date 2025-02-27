package com.example.earthtalk.domain.news.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.example.earthtalk.domain.news.dto.response.NewsListResponse;
import com.example.earthtalk.domain.news.entity.Like;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.NewsType;
import com.example.earthtalk.domain.news.entity.SortType;
import com.example.earthtalk.domain.news.repository.NewsFilterRepository;
import com.example.earthtalk.domain.news.repository.NewsFilterRepositoryImpl;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.global.constant.ContinentType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;

@ExtendWith(MockitoExtension.class)
class NewsDataServiceTest {

    @InjectMocks
    private NewsDataService newsDataService;

    @Mock
    private NewsRepository newsRepository;
    @Mock
    private NewsFilterRepository newsFilterRepository;


    @Test
    void getNewsByFilter() {
        // given
        List<News> newsList = List.of(
            News.builder().id(1L).title("Title 1").content("11111").link("link1")
                .imgUrl("img").newsType(NewsType.HANI).continent(ContinentType.AS)
                .deliveryTime(LocalDateTime.of(2023,2,27,12,0)).build(),
            News.builder().id(2L).title("Title 2").content("22222").link("link2")
                .imgUrl("img").newsType(NewsType.HANI).continent(ContinentType.EU)
                .deliveryTime(LocalDateTime.of(2024,2,27,12,0)).build(),
            News.builder().id(3L).title("Title 3").content("33333").link("link3")
                .imgUrl("img").newsType(NewsType.HANI).continent(ContinentType.AS)
                .deliveryTime(LocalDateTime.of(2025,2,27,12,0)).build()
        );
        given(newsRepository.findAll()).willReturn(newsList);

        // when
        List<NewsListResponse> sortedNews = newsDataService
            .getNewsByFilter(ContinentType.EU, null, SortType.LATEST, null).getContent();
        // then
        Assertions.assertThat(sortedNews.size()).isEqualTo(2);
        Assertions.assertThat(sortedNews.get(0).title()).isEqualTo("Title 3");
        Assertions.assertThat(sortedNews.get(1).title()).isEqualTo("Title 1");
    }

    @Test
    void getNewsDetail() {
    }

    @Test
    void getNewsRanking() {
    }

    @Test
    void addLikeDuplicatedTest() {


    }

    @Test
    void removeLike() {
    }

    @Test
    void addBookmark() {
    }

    @Test
    void removeBookmark() {
    }
}
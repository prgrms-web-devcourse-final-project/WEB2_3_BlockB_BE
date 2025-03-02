package com.example.earthtalk.domain.news.service;

import com.example.earthtalk.domain.news.dto.response.NewsListResponse;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.NewsType;
import com.example.earthtalk.domain.news.entity.SortType;
import com.example.earthtalk.domain.news.repository.NewsFilterRepository;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.global.constant.ContinentType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Transactional
class NewsFilterTest {

    @Autowired
    private NewsDataService newsDataService;

    @Autowired
    private NewsRepository newsRepository;

    @BeforeEach
    void setUp() {
        // given
        List<News> newsList = List.of(
            News.builder().id(1L).title("Title 1").content("11111").link("link1")
                .imgUrl("img").newsType(NewsType.HANI).continent(ContinentType.AS)
                .deliveryTime(LocalDateTime.of(2030, 1, 1, 12, 0)).build(),
            News.builder().id(2L).title("Title 2").content("22222").link("link2")
                .imgUrl("img").newsType(NewsType.HANI).continent(ContinentType.EU)
                .deliveryTime(LocalDateTime.of(2030, 2, 2, 12, 0)).build(),
            News.builder().id(3L).title("Title 3").content("33333").link("link3")
                .imgUrl("img").newsType(NewsType.HANI).continent(ContinentType.AS)
                .deliveryTime(LocalDateTime.of(2030, 3, 3, 12, 0)).build(),
            News.builder().id(4L).title("Title 4").content("44444").link("link4")
                .imgUrl("img").newsType(NewsType.HANI).continent(ContinentType.AS)
                .deliveryTime(LocalDateTime.of(2030, 4, 4, 12, 0)).build()
        );
        newsRepository.saveAll(newsList);
    }

    @Test
    @DisplayName("뉴스 대륙 분류 & 정렬 테스트")
    void getNewsByKeywordAndSort() {
        // when
        List<NewsListResponse> sortedNews = newsDataService
            .getNewsByFilter(ContinentType.AS, "Title", SortType.LATEST, null).getContent();
        // then
        Assertions.assertThat(sortedNews.size()).isEqualTo(3);
        Assertions.assertThat(sortedNews.get(0).title()).isEqualTo("Title 4");
        Assertions.assertThat(sortedNews.get(1).title()).isEqualTo("Title 3");
        Assertions.assertThat(sortedNews.get(2).title()).isEqualTo("Title 1");
    }

    @Test
    @DisplayName("커서 테스트")
    void getNewsByCursor() {
        // when
        List<NewsListResponse> sortedNews = newsDataService
            .getNewsByFilter(ContinentType.AS, "Title", SortType.LATEST, 4L).getContent();
        // then
        Assertions.assertThat(sortedNews.size()).isEqualTo(2);
        Assertions.assertThat(sortedNews.get(0).title()).isEqualTo("Title 3");
        Assertions.assertThat(sortedNews.get(1).title()).isEqualTo("Title 1");
    }

    @Test
    @DisplayName("마지막 페이지 테스트")
    void isLastPage() {
        Slice<NewsListResponse> newsSlice = newsDataService.getNewsByFilter(ContinentType.AS, "Title",
            SortType.LATEST, null);
        Assertions.assertThat(newsSlice.hasNext()).isFalse();
    }
}
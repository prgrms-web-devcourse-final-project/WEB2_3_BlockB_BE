package com.example.earthtalk.domain.news.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.earthtalk.domain.news.dto.response.NewsDetailResponse;
import com.example.earthtalk.domain.news.entity.Like;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.repository.LikeRepository;
import com.example.earthtalk.domain.news.repository.NewsFilterRepository;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.BadRequestException;
import com.example.earthtalk.global.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NewsDataService newsDataService;

    private final Long newsId = 1L;
    private final Long userId = 10L;
    private final String newsLink = "https://example.com/news/1";


    @Test
    @DisplayName("뉴스 상세보기 (유저 ID 포함)")
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
    @DisplayName("뉴스 상세보기")
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
    @DisplayName("NotFound 테스트")
    void newsDoNotExist() {
        when(newsRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> newsDataService.getNewsDetail(2L, userId));
    }

    @Test
    @DisplayName("좋아요 성공 테스트")
    void addLikeSuccess() {
        // Given
        Long newsId = 1L;
        Long userId = 2L;
        User user = User.builder().nickname("TestUser").build();
        News news = News.builder().id(newsId).title("TestNews").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(news));
        when(likeRepository.existsByUserIdAndNewsId(userId, newsId)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> newsDataService.addLike(newsId, userId));

        // Then
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("좋아요 중복 테스트")
    void addLikeDuplicateTest() {
        // Given
        Long newsId = 1L;
        Long userId = 2L;
        User user = User.builder().nickname("TestUser").build();
        News news = News.builder().id(newsId).title("TestNews").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(news));
        when(likeRepository.existsByUserIdAndNewsId(userId, newsId)).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> newsDataService.addLike(newsId, userId));
    }


}

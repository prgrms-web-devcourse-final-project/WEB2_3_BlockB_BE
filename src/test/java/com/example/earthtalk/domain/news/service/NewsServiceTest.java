package com.example.earthtalk.domain.news.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.earthtalk.domain.news.entity.Like;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.repository.LikeRepository;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.BadRequestException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @InjectMocks
    private NewsDataService newsDataService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private LikeRepository likeRepository;

    @Test
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


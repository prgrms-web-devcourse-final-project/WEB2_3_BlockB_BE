package com.example.earthtalk.domain.news.service;

import com.example.earthtalk.domain.news.dto.NewsDetailDTO;
import com.example.earthtalk.domain.news.dto.NewsListDTO;
import com.example.earthtalk.domain.news.entity.Bookmark;
import com.example.earthtalk.domain.news.entity.Like;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.repository.BookmarkRepository;
import com.example.earthtalk.domain.news.repository.LikeRepository;
import com.example.earthtalk.domain.news.repository.NewsFilterRepository;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsDataService {

    private final NewsFilterRepository newsFilterRepository;
    private final NewsRepository newsRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    public Slice<NewsListDTO> getNewsByFilter(String continent, String query, String sort,
        Long newsId) {
        return newsFilterRepository.filterNews(continent, query, sort, newsId);
    }

    public NewsDetailDTO getNewsDetail(Long newsId, Long userId) {
        News news = newsRepository.findById(newsId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NEWS_NOT_FOUND));
        Long like = newsRepository.countNewsLike(newsId);
        Long mark = newsRepository.countNewsBookmark(newsId);
        boolean liked = false;
        boolean marked = false;
        if (userId != null) {
            liked = newsFilterRepository.isLiked(userId, newsId);
            marked = newsFilterRepository.isMarked(userId, newsId);
        }
        return new NewsDetailDTO(like, mark, news.getLink(), marked, liked);
    }

    public List<NewsListDTO> getNewsRanking() {
        return newsFilterRepository.newsRanking();
    }

    public void addLike(Long newsId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        News news = newsRepository.findById(newsId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NEWS_NOT_FOUND));

        Like like = Like.builder().news(news).user(user).build();
        likeRepository.save(like);
    }

    public void removeLike(Long userId, Long newsId) {
        likeRepository.deleteByUserIdAndNewsId(userId, newsId);
    }

    public void addBookmark(Long newsId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        News news = newsRepository.findById(newsId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NEWS_NOT_FOUND));

        Bookmark mark = Bookmark.builder().news(news).user(user).build();
        bookmarkRepository.save(mark);
    }

    public void removeBookmark(Long userId, Long newsId) {
        bookmarkRepository.deleteByUserIdAndNewsId(userId, newsId);
    }

}

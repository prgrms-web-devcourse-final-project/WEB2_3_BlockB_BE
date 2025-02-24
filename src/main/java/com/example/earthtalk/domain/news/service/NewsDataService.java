package com.example.earthtalk.domain.news.service;

import com.example.earthtalk.domain.news.dto.response.NewsDetailReponse;
import com.example.earthtalk.domain.news.dto.response.NewsListResponse;
import com.example.earthtalk.domain.news.entity.Bookmark;
import com.example.earthtalk.domain.news.entity.Like;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.QLike;
import com.example.earthtalk.domain.news.entity.QNews;
import com.example.earthtalk.domain.news.repository.BookmarkRepository;
import com.example.earthtalk.domain.news.repository.LikeRepository;
import com.example.earthtalk.domain.news.repository.NewsFilterRepository;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.constant.ContinentType;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsDataService {

    private final NewsFilterRepository newsFilterRepository;
    private final NewsRepository newsRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    private static final int PAGE_SIZE = 12;

    public Slice<NewsListResponse> getNewsByFilter(String continent, String query, String sort,
        Long newsId) {
        QNews news = QNews.news;
        QLike like = QLike.like;

        BooleanBuilder whereBuilder = new BooleanBuilder();
        BooleanBuilder havingBuilder = new BooleanBuilder();

        if (continent != null && !continent.isEmpty()) {
            try {
                whereBuilder.and(news.continent.eq(ContinentType.valueOf(continent)));
            } catch (Exception e) {
                throw new NotFoundException(ErrorCode.CONTINENT_NOT_FOUND);
            }
        }
        if (query != null && !query.isEmpty()) {
            whereBuilder.and(news.title.containsIgnoreCase(query)
                .or(news.content.containsIgnoreCase(query)));
        }

        OrderSpecifier<?> orderSpecifier = news.deliveryTime.desc();

        if (sort == null || sort.equals("latest")) {
            LocalDateTime deliveryTimeOfLastNews = newsRepository.getNewsDeliveryTime(newsId);
            orderSpecifier = news.deliveryTime.desc();
            if (newsId != null) {
                whereBuilder.and(news.deliveryTime.lt(deliveryTimeOfLastNews)) // 마지막 뉴스보다 더 이전에 작성된
                    .or(news.deliveryTime.eq(deliveryTimeOfLastNews)
                        .and(news.id.lt(newsId))); // 작성시간 같을경우 아이디로 비교
            }

        } else if (sort.equals("popular")) {
            Long likesOfLastNews = newsRepository.countNewsLike(newsId);
            orderSpecifier = like.count().desc();
            if (newsId != null) {
                havingBuilder.and(like.count().lt(likesOfLastNews)) // 라이크 갯수가 마지막 뉴스보다 적은
                    .or(like.count().eq(likesOfLastNews)
                        .and(news.id.lt(newsId))); // 라이크 갯수가 같을 경우 아이디로 비교
            }
        }

        List<NewsListResponse> newsList = newsFilterRepository.filterNews(whereBuilder,
            havingBuilder,
            orderSpecifier, PAGE_SIZE);

        boolean hasNextPage = false;
        if (newsList.size() > PAGE_SIZE) {
            hasNextPage = true;
            newsList.remove(newsList.size() - 1);
        }

        return new SliceImpl<>(newsList, PageRequest.of(0, PAGE_SIZE), hasNextPage);
    }

    public NewsDetailReponse getNewsDetail(Long newsId, Long userId) {
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
        return new NewsDetailReponse(like, mark, news.getLink(), marked, liked);
    }

    public List<NewsListResponse> getNewsRanking() {
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

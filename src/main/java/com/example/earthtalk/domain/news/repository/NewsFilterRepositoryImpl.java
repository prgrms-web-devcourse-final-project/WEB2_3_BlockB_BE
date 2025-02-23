package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.dto.NewsListDTO;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.QBookmark;
import com.example.earthtalk.domain.news.entity.QLike;
import com.example.earthtalk.domain.news.entity.QNews;
import com.example.earthtalk.domain.news.service.NewsDataService;
import com.example.earthtalk.global.constant.ContinentType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryFactory;
import com.querydsl.core.Tuple;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsFilterRepositoryImpl implements NewsFilterRepository {

    private final NewsRepository newsRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<NewsListDTO> filterNews(String continent, String query, String sort,
        Long newsId) {
        QNews news = QNews.news;
        QLike like = QLike.like;
        QBookmark bookmark = QBookmark.bookmark;
        BooleanBuilder builder = new BooleanBuilder();

        if (continent != null && !continent.isEmpty()) {
            builder.and(news.continent.eq(ContinentType.valueOf(continent)));
        }
        if (query != null && !query.isEmpty()) {
            builder.and(news.title.containsIgnoreCase(query)
                .or(news.content.containsIgnoreCase(query)));
        }

        OrderSpecifier<?> orderSpecifier = news.deliveryTime.desc();

        if (sort == null || sort.equals("latest")) {
            LocalDateTime deliveryTimeOfLastNews = newsRepository.getNewsDeliveryTime(newsId);
            orderSpecifier = news.deliveryTime.desc();
            if(newsId != null) {
                builder.and(news.deliveryTime.lt(deliveryTimeOfLastNews)) // 마지막 뉴스보다 더 이전에 작성된
                    .or(news.deliveryTime.eq(deliveryTimeOfLastNews).and(news.id.gt(newsId))); // 작성시간 같을경우 아이디로 비교
            }

        } else if (sort.equals("popular")) {
            Long likesOfLastNews = newsRepository.countNewsLike(newsId);
            orderSpecifier = like.count().desc();
            if(newsId != null) {
                builder.and(like.count().gt(likesOfLastNews)) // 라이크 갯수가 마지막 뉴스보다 많은
                    .or(like.count().eq(likesOfLastNews)
                        .and(news.id.gt(newsId))); // 라이크 갯수가 같을 경우 아이디로 비교
            }
        }

        List<NewsListDTO> newsList = jpaQueryFactory
            .select(Projections.constructor(NewsListDTO.class,
                news, like.count(), bookmark.count()))
            .from(news)
            .leftJoin(like).on(news.id.eq(like.news.id))
            .leftJoin(bookmark).on(news.id.eq(bookmark.news.id))
            .where(builder)
            .groupBy(news.id)
            .orderBy(orderSpecifier) // sort 쿼리파라미터에 따라 다르게 정렬
            .orderBy(news.id.desc()) // 비교값이 같을 경우 id 순서로 정렬
            .limit(12 + 1)
            .fetch();

        boolean hasNextPage = false;
        if (newsList.size() > 12) {
            hasNextPage = true;
            newsList.remove(newsList.size() - 1);
        }

        return new SliceImpl<>(newsList, PageRequest.of(0, 12), hasNextPage);
    }

}

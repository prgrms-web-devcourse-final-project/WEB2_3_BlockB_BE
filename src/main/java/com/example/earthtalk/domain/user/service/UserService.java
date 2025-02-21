package com.example.earthtalk.domain.user.service;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.domain.user.dto.response.UserBookmarksResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebatesResponse;
import com.example.earthtalk.domain.user.dto.response.UserLikesResponse;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.domain.user.dto.response.UserInfoResponse;
import com.example.earthtalk.global.constant.ContinentType;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.querydsl.core.Tuple;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final DebateRepository debateRepository;

    // 모든 유저 정보 조회(인기순) / 유저 검색
    public List<UserInfoResponse> getPopularUsersInfo(String query) {
        List<Tuple> userFollowFollowerData = userRepository.findAllWithFollowCountOrderBy(query);
        System.out.println(userFollowFollowerData);

        List<UserInfoResponse> userInfoDTOList = new ArrayList<>();

        for (Tuple data : userFollowFollowerData) {
            Long userId = data.get(0, Long.class);
            Long totalFollowers = data.get(1, Long.class);
            Long totalFollowees = data.get(2, Long.class);

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

            userInfoDTOList.add(UserInfoResponse.from(user, totalFollowers, totalFollowees));
        }

        return userInfoDTOList;
    }

    // 유저가 좋아요한 뉴스 목록 조회
    public List<UserLikesResponse> getUserLikes(Long userId) {
        List<Tuple> userLikesData = userRepository.findAllWithLikes(userId);
        System.out.println(userLikesData);
        List<UserLikesResponse> userLikesDTOList = new ArrayList<>();

        for ( Tuple data : userLikesData ) {
            Long newsId = data.get(1, Long.class);
            boolean isLike = Boolean.TRUE.equals(data.get(2, Boolean.class));
            String title = data.get(3, String.class);
            ContinentType continent = ContinentType.valueOf(
                String.valueOf(data.get(4, ContinentType.class)));
            LocalDateTime createdAt = data.get(5, LocalDateTime.class);

            userLikesDTOList.add(new UserLikesResponse(newsId, isLike, title, continent, createdAt));
        }

        return userLikesDTOList;
    }

    // 유저가 북마크한 뉴스 목록 조회
    public List<UserBookmarksResponse> getUserBookmarks(Long userId) {
        List<Tuple> userBookmarksData = userRepository.findAllWithBookmarks(userId);
        System.out.println(userBookmarksData);
        List<UserBookmarksResponse> userBookmarksDTOList = new ArrayList<>();

        for ( Tuple data : userBookmarksData ) {
            Long newsId = data.get(1, Long.class);
            boolean isBookmarked = Boolean.TRUE.equals(data.get(2, Boolean.class));
            String title = data.get(3, String.class);
            ContinentType continent = ContinentType.valueOf(
                String.valueOf(data.get(4, ContinentType.class)));
            LocalDateTime createdAt = data.get(5, LocalDateTime.class);

            userBookmarksDTOList.add(new UserBookmarksResponse(newsId, isBookmarked, title, continent, createdAt));
        }

        return userBookmarksDTOList;
    }

    // 유저가 참여한 토론방 목록 조회
    public List<UserDebatesResponse> getUserDebates(Long userId) {
        List<Tuple> userDebatesData = userRepository.findAllWithDebates(userId);
        System.out.println("userDebatesData: " + userDebatesData);
        List<UserDebatesResponse> userDebatesDTOList = new ArrayList<>();

        for ( Tuple data : userDebatesData ) {
            Long debateId = data.get(0, Long.class);
            CategoryType category = CategoryType.valueOf(
                String.valueOf(data.get(1, CategoryType.class)));
            String title = data.get(2, String.class);
            TimeType time = TimeType.valueOf(
                String.valueOf(data.get(3, CategoryType.class)));
            MemberNumberType member = MemberNumberType.valueOf(
                String.valueOf(data.get(4, MemberNumberType.class)));
            RoomType status = RoomType.valueOf(
                String.valueOf(data.get(6, RoomType.class)));


            userDebatesDTOList.add(new UserDebatesResponse(debateId, category, title, time, member, userId, status));
        }

        return userDebatesDTOList;
    }
}

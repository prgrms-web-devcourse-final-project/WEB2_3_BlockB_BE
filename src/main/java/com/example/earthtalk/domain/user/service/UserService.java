package com.example.earthtalk.domain.user.service;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateRole;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.news.entity.Bookmark;
import com.example.earthtalk.domain.news.entity.Like;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.news.repository.BookmarkRepository;
import com.example.earthtalk.domain.news.repository.LikeRepository;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.domain.user.dto.response.UserBookmarksResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebateChatsResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebateDetailsResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebatesResponse;
import com.example.earthtalk.domain.user.dto.response.UserFolloweesResponse;
import com.example.earthtalk.domain.user.dto.response.UserFollowersResponse;
import com.example.earthtalk.domain.user.dto.response.UserLikesResponse;
import com.example.earthtalk.domain.user.entity.Follow;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.FollowRepository;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.domain.user.dto.response.UserInfoResponse;
import com.example.earthtalk.global.constant.ContinentType;
import com.example.earthtalk.global.exception.ConflictException;
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
    private final DebateRepository debateRepository;
    private final NewsRepository newsRepository;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final FollowRepository followRepository;

    //유저 조회
    public User getUSerInfo(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    // 모든 유저 정보 조회(인기순) / 유저 검색
    public List<UserInfoResponse> getPopularUsersInfo(String query) {
        List<Tuple> userFollowFollowerData = userRepository.findAllWithFollowCountOrderBy(query);

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

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        List<Tuple> userLikesData = userRepository.findAllWithLikes(userId);

        List<UserLikesResponse> userLikesDTOList = new ArrayList<>();

        for ( Tuple data : userLikesData ) {
            Long newsId = data.get(1, Long.class);
            String title = data.get(2, String.class);
            ContinentType continent = ContinentType.valueOf(
                String.valueOf(data.get(3, ContinentType.class)));
            LocalDateTime createdAt = data.get(4, LocalDateTime.class);

            userLikesDTOList.add(new UserLikesResponse(newsId, title, continent, createdAt));
        }

        return userLikesDTOList;
    }

    // 유저 좋아요 추가
    public void insertLike(Long newsId, Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        News news = newsRepository.findById(newsId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NEWS_NOT_FOUND));

        // 좋아요 중복 방지
        boolean alreadyLiked = likeRepository.existsByUserIdAndNewsId(userId, newsId);

        if (alreadyLiked) {
            throw new ConflictException(ErrorCode.ALREADY_LIKED);
        }

        Like like = Like.builder().user(user).news(news).build();

        likeRepository.save(like);
    }

    // 유저 좋아요 삭제
    public void deleteLike(Long newsId, Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        News news = newsRepository.findById(newsId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NEWS_NOT_FOUND));

        likeRepository.deleteByUserIdAndNewsId(userId, newsId);

    }

    // 유저가 북마크한 뉴스 목록 조회
    public List<UserBookmarksResponse> getUserBookmarks(Long userId) {
        List<Tuple> userBookmarksData = userRepository.findAllWithBookmarks(userId);

        List<UserBookmarksResponse> userBookmarksDTOList = new ArrayList<>();

        for ( Tuple data : userBookmarksData ) {
            Long newsId = data.get(1, Long.class);
            String title = data.get(2, String.class);
            ContinentType continent = ContinentType.valueOf(
                String.valueOf(data.get(3, ContinentType.class)));
            LocalDateTime createdAt = data.get(4, LocalDateTime.class);

            userBookmarksDTOList.add(new UserBookmarksResponse(newsId, title, continent, createdAt));
        }

        return userBookmarksDTOList;
    }

    // 유저 북마크 추가
    public void insertBookmark(Long newsId, Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        News news = newsRepository.findById(newsId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NEWS_NOT_FOUND));

        // 북마크 중복 방지
        boolean alreadyBookmarked = bookmarkRepository.existsByUserIdAndNewsId(userId, newsId);

        if (alreadyBookmarked) {
            throw new ConflictException(ErrorCode.ALREADY_BOOKMARKED);
        }

        Bookmark bookmark = Bookmark.builder().user(user).news(news).build();

        bookmarkRepository.save(bookmark);
    }

    // 유저 북마크 삭제
    public void deleteBookmark(Long newsId, Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        News news = newsRepository.findById(newsId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NEWS_NOT_FOUND));

        bookmarkRepository.deleteByUserIdAndNewsId(userId, newsId);
    }

    // 유저가 참여/참관한 토론방 목록 조회
    public List<UserDebatesResponse> getUserDebates(Long userId) {

        List<Tuple> userDebatesData = userRepository.findAllWithDebates(userId);

        List<UserDebatesResponse> userDebatesDTOList = new ArrayList<>();

        for ( Tuple data : userDebatesData ) {
            Long debateId = data.get(0, Long.class); // 인덱스 0
            CategoryType category = data.get(1, CategoryType.class); // 인덱스 1
            String title = data.get(2, String.class); // 인덱스 2
            TimeType time = data.get(3, TimeType.class); // 인덱스 3
            MemberNumberType member = data.get(4, MemberNumberType.class); // 인덱스 4
            RoomType status = data.get(5, RoomType.class); // 인덱스 5
            Boolean isParticipant = data.get(6, Boolean.class); // 인덱스 6


            userDebatesDTOList.add(new UserDebatesResponse(debateId, category, title, time, member, status, isParticipant));
        }

        return userDebatesDTOList;
    }

    // 유저가 참여/참관한 토론방 상세 조회 - header
    public List<UserDebateDetailsResponse> getDebateDetails(Long debatesId) {
        List<Tuple> debateDetailsData = userRepository.findAllWithDebateDetails(debatesId);

        List<UserDebateDetailsResponse> userDebateDetailsDTOList = new ArrayList<>();

        for (Tuple data : debateDetailsData) {
            String link = data.get(3, String.class);

            Debate debate = debateRepository.findById(debatesId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DEBATEROOM_NOT_FOUND));

            userDebateDetailsDTOList.add(UserDebateDetailsResponse.from(debate, link));
        }
        System.out.println(userDebateDetailsDTOList);
        return userDebateDetailsDTOList;
    }

    // 유저가 참여/참관한 토론방 상세 조회 - body
    public List<UserDebateChatsResponse> getUserDebateChats(Long debatesId) {
        List<Tuple> userDebateChatsData = userRepository.findAllWithDebateChats(debatesId);

        List<UserDebateChatsResponse> userDebateChatsDTOList = new ArrayList<>();

        for ( Tuple data : userDebateChatsData ) {
            Long userId = data.get(0, Long.class);
            DebateRole role = DebateRole.valueOf(
                String.valueOf(data.get(1, DebateRole.class)));
            FlagType position = FlagType.valueOf(
                String.valueOf(data.get(2, FlagType.class)));
            String debateContent = data.get(3, String.class);
            LocalDateTime createdAt = data.get(4, LocalDateTime.class);


            userDebateChatsDTOList.add(new UserDebateChatsResponse(
                userId, role, position, debateContent, createdAt));
        }

        return userDebateChatsDTOList;
    }



    // 유저 팔로우/필로워 조회
    public List<UserFolloweesResponse> getUserFollowees(Long userId) {
        List<Tuple> userFolloweesData = userRepository.findAllWithFollowees(userId);

        List<UserFolloweesResponse> userFolloweesDTOList = new ArrayList<>();

        for ( Tuple data : userFolloweesData ) {
            Long followeeId = data.get(0, Long.class);
            String nickname = data.get(1, String.class);
            String profile = data.get(2, String.class);
            String introduction = data.get(3, String.class);

            userFolloweesDTOList.add(new UserFolloweesResponse(followeeId, nickname, profile, introduction));
        }

        return userFolloweesDTOList;
    }

    // 유저 팔로우/필로워 조회
    public List<UserFollowersResponse> getUserFollowers(Long userId) {
        List<Tuple> userFollowersData = userRepository.findAllWithFollowers(userId);

        List<UserFollowersResponse> userFollowersDTOList = new ArrayList<>();

        for ( Tuple data : userFollowersData ) {
            Long followerId = data.get(0, Long.class);
            String nickname = data.get(1, String.class);
            String profile = data.get(2, String.class);
            String introduction = data.get(3, String.class);

            userFollowersDTOList.add(new UserFollowersResponse(followerId, nickname, profile, introduction));
        }

        return userFollowersDTOList;
    }


    //유저 팔로워/팔로잉 추가
    public void insertUserFollows(Long followeeId, Long followerId) {

        User followee = userRepository.findById(followeeId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        User follower = userRepository.findById(followerId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Follow follow = Follow.builder().followee(followee).follower(follower).build();

        followRepository.save(follow);
    }

    //유저 팔로워/팔로잉 삭제
    public void deleteUserFollows(Long followeeId, Long followerId) {

        User followee = userRepository.findById(followeeId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        User follower = userRepository.findById(followerId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        followRepository.deleteByFollows(followeeId, followerId);
    }

    //유저 프로필 수정
    public void updateUsers(String nickname, String introduction, String profile, Long userId) {

        userRepository.updateUserById(nickname, introduction, profile, userId);
    }



    public User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}

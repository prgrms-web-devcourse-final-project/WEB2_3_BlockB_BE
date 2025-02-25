package com.example.earthtalk.domain.user.service;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateRole;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.domain.user.dto.response.UserBookmarksResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebateChatsResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebateDetailsResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebatesResponse;
import com.example.earthtalk.domain.user.dto.response.UserFolloweesResponse;
import com.example.earthtalk.domain.user.dto.response.UserFollowersResponse;
import com.example.earthtalk.domain.user.dto.response.UserLikesResponse;
import com.example.earthtalk.domain.user.dto.response.UserObserverChatsResponse;
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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DebateRepository debateRepository;

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
    public int insertLike(Long newsId, Long userId) {

        int flag = userRepository.insertLike(newsId, userId);

        if ( flag == 0 ) {
            return 1;
        } else {
            return 0;
        }
    }

    // 유저 좋아요 삭제
    public int deleteLike(Long newsId, Long userId) {

        int flag = userRepository.deleteLikeByUserId(newsId, userId);

        if ( flag == 0 ) {
            return 1;
        } else {
            return 0;
        }
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
    public int insertBookmark(Long newsId, Long userId) {

        int flag = userRepository.insertBookmark(newsId, userId);

        if ( flag == 0 ) {
            return 1;
        } else {
            return 0;
        }
    }

    // 유저 북마크 삭제
    public int deleteBookmark(Long newsId, Long userId) {

        int flag = userRepository.deleteBookmarkByUserId(newsId, userId);

        if ( flag == 0 ) {
            return 1;
        } else {
            return 0;
        }
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
    public int insertUserFollows(Long followeeId, Long followerId) {

        int flag = userRepository.insertFollows(followeeId, followerId);

        if ( flag == 0 ) {
            return 1;
        } else {
            return 0;
        }
    }

    //유저 팔로워/팔로잉 삭제
    public int deleteUserFollows(Long followeeId, Long followerId) {

        int flag = userRepository.deleteByFollows(followeeId, followerId);

        if ( flag == 0 ) {
            return 1;
        } else {
            return 0;
        }
    }

    //유저 프로필 수정
    public int updateUsers(String nickname, String introduction, String profile, Long userId) {

        int flag = userRepository.updateUserById(nickname, introduction, profile, userId);

        if ( flag == 0 ) {
            return 1;
        } else {
            return 0;
        }
    }



    public User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}

package com.example.earthtalk.domain.user.repository;

import com.example.earthtalk.domain.user.entity.SocialType;
import jakarta.transaction.Transactional;
import java.util.Optional;
import com.example.earthtalk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByNickname(String nickname);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO likes (created_at, updated_at, is_like, news_id, user_id) VALUES (NOW(), NOW(), 1, :newsId, :userId);", nativeQuery = true)
    int insertLike(@Param("newsId") Long newsId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO bookmarks (created_at, updated_at, is_bookmarked, news_id, user_id) VALUES (NOW(), NOW(), 1, :newsId, :userId);", nativeQuery = true)
    int insertBookmark(@Param("newsId") Long newsId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO follows (followee_id, follower_id) VALUES (:followeeId, :followerId)", nativeQuery = true)
    int insertFollows(@Param("followeeId") Long followeeId, @Param("followerId") Long followerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM follows f WHERE f.followee.id = :followeeId AND f.follower.id = :followerId")
    int deleteByFollows(@Param("followeeId") long followeeId, @Param("followerId") long followerId);

    @Modifying
    @Transactional
    @Query("UPDATE users u SET u.nickname = :nickname, u.introduction = :introduction, u.profileUrl = :profile WHERE u.id = :userId")
    int updateUserById(@Param("nickname") String nickname,
        @Param("introduction") String introduction,
        @Param("profile") String profile,
        @Param("userId") Long userId);


    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    Optional<User> findByEmail(String email);
}

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
    @Query("UPDATE users u SET u.nickname = :nickname, u.introduction = :introduction, u.profileUrl = :profile WHERE u.id = :userId")
    void updateUserById(@Param("nickname") String nickname,
        @Param("introduction") String introduction,
        @Param("profile") String profile,
        @Param("userId") Long userId);

    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);
}

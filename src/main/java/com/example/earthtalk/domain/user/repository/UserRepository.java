package com.example.earthtalk.domain.user.repository;

import com.example.earthtalk.domain.user.entity.SocialType;
import java.util.Optional;
import com.example.earthtalk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    Optional<User> findByNickname(String nickname);

    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);
}

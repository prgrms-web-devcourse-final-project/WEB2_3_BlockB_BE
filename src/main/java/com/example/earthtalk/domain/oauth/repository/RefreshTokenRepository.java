package com.example.earthtalk.domain.oauth.repository;

import com.example.earthtalk.domain.oauth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    boolean existsByUserEmail(String userEmail);

    Optional<RefreshToken> findByUserEmail(String userEmail);
}

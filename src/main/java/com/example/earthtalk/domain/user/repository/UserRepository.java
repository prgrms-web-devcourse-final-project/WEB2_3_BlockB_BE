package com.example.earthtalk.domain.user.repository;

import com.example.earthtalk.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    @Query("SELECT u FROM users u WHERE u.nickname LIKE %:nickname%")
    List<User> findByNicknameContaining(String nickname);

}

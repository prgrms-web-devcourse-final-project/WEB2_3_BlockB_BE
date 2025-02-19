package com.example.earthtalk.domain.user.repository;

import com.example.earthtalk.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.id AS userId, " +
        "COALESCE(COUNT(DISTINCT f1.follower.id), 0) AS totalFollowers, " +
        "COALESCE(COUNT(DISTINCT f2.followee.id), 0) AS totalFollowees " +
        "FROM users u " +
        "LEFT JOIN follows f1 ON u.id = f1.followee.id " +
        "LEFT JOIN follows f2 ON u.id = f2.follower.id " +
        "GROUP BY u.id " +
        "ORDER BY totalFollowers DESC")
    List<Object[]> getFollowerFolloweeCount();

}

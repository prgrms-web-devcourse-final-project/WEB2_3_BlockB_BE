package com.example.earthtalk.domain.user.repository;

import com.example.earthtalk.domain.user.entity.User;
import com.querydsl.core.Tuple;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface UserRepositoryCustom {
    List<Tuple> findAllWithFollowCountOrderBy(String query);
}

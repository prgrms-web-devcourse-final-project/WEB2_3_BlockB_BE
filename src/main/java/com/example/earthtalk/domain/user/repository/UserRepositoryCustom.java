package com.example.earthtalk.domain.user.repository;

import com.querydsl.core.Tuple;
import java.util.List;

public interface UserRepositoryCustom {
    List<Tuple> findAllWithFollowCountOrderBy(String query);

    List<Tuple> findAllWithLikes(Long userId);

    List<Tuple> findAllWithBookmarks(Long userId);

    List<Tuple> findAllWithDebates(Long userId);
}

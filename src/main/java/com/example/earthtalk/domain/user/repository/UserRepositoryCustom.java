package com.example.earthtalk.domain.user.repository;

import com.querydsl.core.Tuple;
import java.util.List;
import java.util.UUID;

public interface UserRepositoryCustom {
    List<Tuple> findAllWithFollowCountOrderBy(String query);

    List<Tuple> findAllWithLikes(Long userId);

    List<Tuple> findAllWithBookmarks(Long userId);

    List<Tuple> findAllWithDebates(Long userId);

    List<Tuple> findAllWithDebateDetails(UUID debateId);

    List<Tuple> findAllWithDebateChats(Long debateId);

    List<Tuple> findAllWithFollowees(Long userId);

    List<Tuple> findAllWithFollowers(Long userId);
}

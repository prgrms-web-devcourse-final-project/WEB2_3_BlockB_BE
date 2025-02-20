package com.example.earthtalk.domain.user.repository;

import com.querydsl.core.Tuple;
import java.util.List;

public interface UserRepositoryCustom {
    List<Tuple> findAllWithFollowCountOrderBy(String query);
}

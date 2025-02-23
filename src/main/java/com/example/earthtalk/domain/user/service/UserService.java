package com.example.earthtalk.domain.user.service;

import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.domain.user.dto.response.UserInfoResponse;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.querydsl.core.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 모든 유저 정보 조회(인기순) / 유저 검색
    public List<UserInfoResponse> getPopularUsersInfo(String query) {
        List<Tuple> userFollowFollowerData = userRepository.findAllWithFollowCountOrderBy(query);


        List<UserInfoResponse> userInfoDTOList = new ArrayList<>();

        for (Tuple data : userFollowFollowerData) {
            Long userId = data.get(0, Long.class);
            Long totalFollowers = data.get(1, Long.class);
            Long totalFollowees = data.get(2, Long.class);

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

            userInfoDTOList.add(UserInfoResponse.from(user, totalFollowers, totalFollowees));
        }

        return userInfoDTOList;
    }

    public User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}

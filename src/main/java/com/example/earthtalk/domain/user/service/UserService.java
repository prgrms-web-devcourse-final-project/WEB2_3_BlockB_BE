package com.example.earthtalk.domain.user.service;

import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.domain.user.userDto.UserInfoDto;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.GlobalExceptionHandler;
import com.example.earthtalk.global.exception.NotFoundException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.NoHandlerFoundException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 모든 유저 정보 조회(인기순)
    public List<UserInfoDto> getPopularUsersInfo() {
        List<Object[]> userFollowFollowerData = userRepository.getFollowerFolloweeCount();
        List<UserInfoDto> userInfoDTOList = new ArrayList<>();

        for (Object[] data : userFollowFollowerData) {
            Long userId = (Long) data[0];
            Long totalFollowers = (Long) data[1];
            Long totalFollowees = (Long) data[2];

            User userInfo = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

            UserInfoDto dto = new UserInfoDto(
                userId,
                userInfo.getNickname(),
                userInfo.getProfile(),
                userInfo.getIntroduction(),
                totalFollowers,
                totalFollowees,
                userInfo.getWinNumber(),
                userInfo.getDrawNumber(),
                userInfo.getDefeatNumber()
            );

            userInfoDTOList.add(dto);
        }
        return userInfoDTOList;
    }
}

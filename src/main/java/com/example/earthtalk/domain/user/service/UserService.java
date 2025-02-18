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

    //private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public Map<String, Object> getAllUsersInfo() {

        Map<String, Object> response = null;
        try {
            List<Object[]> userFollowFollowerData = userRepository.getFollowerFolloweeCount();
            List<UserInfoDto> userInfoDTOList = new ArrayList<>();

            for (Object[] data : userFollowFollowerData) {
                Long userId = (Long) data[0];
                Long totalFollowers = (Long) data[1];
                Long totalFollowees = (Long) data[2];

                Optional<User> userInfo = userRepository.findById(userId);

                UserInfoDto dto = new UserInfoDto();
                dto.setUserId(userId);
                dto.setNickname(userInfo.get().getNickname());
                dto.setProfile(userInfo.get().getProfile());
                dto.setIntroduction(userInfo.get().getIntroduction());
                dto.setTotalFollowers(totalFollowers);
                dto.setTotalFollowees(totalFollowees);
                dto.setWins(userInfo.get().getWinNumber());
                dto.setDraws(userInfo.get().getDrawNumber());
                dto.setLosses(userInfo.get().getDefeatNumber());

                userInfoDTOList.add(dto);

            }

            response = new LinkedHashMap<>();
            response.put("status", "성공");
            response.put("message", null);
            response.put("data", userInfoDTOList);
        } catch (NotFoundException e) {
            ErrorCode errorCode = ErrorCode.NOT_FOUND; //400 "유효하지 않은 사용자 정보입니다."
            response.put("status", "실패");
            response.put("message", errorCode.getMessage());
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.USER_NOT_FOUND; //404 "해당 검색어에 대한 유저 정보가 없습니다..;;"
            response.put("status", "실패");
            response.put("message", errorCode.getMessage());
        }

        return response;
    }
}

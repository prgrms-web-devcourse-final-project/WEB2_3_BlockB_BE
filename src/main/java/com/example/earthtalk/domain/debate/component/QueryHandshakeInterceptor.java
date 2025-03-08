package com.example.earthtalk.domain.debate.component;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.global.constant.ContinentType;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		if (request instanceof ServletServerHttpRequest servletRequest) {
			HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
			String continentParam = httpServletRequest.getParameter("continent");
			String categoryParam = httpServletRequest.getParameter("category");
			String memberParam = httpServletRequest.getParameter("member");

			if (continentParam != null && !continentParam.trim().isEmpty()) {
				try {
					ContinentType continentType = ContinentType.valueOf(continentParam);
					attributes.put("continentType", continentType);
					log.info("Continent type is {}", continentType);
				} catch (IllegalArgumentException e) {
					log.warn("Invalid continent type: {}", continentParam);
				}
			}
			if (categoryParam != null && !categoryParam.trim().isEmpty()) {
				try {
					CategoryType categoryType = CategoryType.valueOf(categoryParam);
					attributes.put("categoryType", categoryType);
					log.info("Category type is {}", categoryType);
				} catch (IllegalArgumentException e) {
					log.warn("Invalid category type: {}", categoryParam);
				}
			}

			if (memberParam != null && !memberParam.trim().isEmpty()) {
				try {
					MemberNumberType memberNumberType = MemberNumberType.valueOf(memberParam);
					attributes.put("memberNumberType", memberNumberType);
					log.info("Member number type is {}", memberNumberType);
				} catch (IllegalArgumentException e) {
					log.warn("Invalid member number type: {}", memberParam);
				}
			}
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {

	}
}

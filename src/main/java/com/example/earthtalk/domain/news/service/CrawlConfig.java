package com.example.earthtalk.domain.news.service;

import com.example.earthtalk.domain.news.entity.NewsSite;
import com.example.earthtalk.domain.news.entity.NewsType;
import com.example.earthtalk.global.constant.ContinentType;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Configuration
@Getter
@ConfigurationProperties("news")
@RequiredArgsConstructor
public class CrawlConfig {

    private final List<NewsSite> sites;
}

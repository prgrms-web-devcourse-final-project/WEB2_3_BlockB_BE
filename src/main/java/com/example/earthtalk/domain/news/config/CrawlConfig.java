package com.example.earthtalk.domain.news.config;

import com.example.earthtalk.domain.news.entity.NewsSite;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@ConfigurationProperties("news")
@RequiredArgsConstructor
public class CrawlConfig {

    private final List<NewsSite> sites;
}

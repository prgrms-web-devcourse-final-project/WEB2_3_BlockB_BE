package com.example.earthtalk.domain.news.entity;

import com.example.earthtalk.global.constant.ContinentType;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class NewsSite {

    private NewsType name;
    private String baseUrl;
    private Map<ContinentType, String> continentUrl;
    private String articleXpath;
    private String urlXpath;
    private String titleXpath;
    private String contentXpath;
    private String dateXpath;
    private String imgXpath;

    private String pageParam;
    private String dateSeparator;
}
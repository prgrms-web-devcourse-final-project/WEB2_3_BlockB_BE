package com.example.earthtalk.domain.news.service;

import com.example.earthtalk.domain.news.entity.News.NewsBuilder;
import com.example.earthtalk.domain.news.entity.NewsSite;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.NewsType;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.global.constant.ContinentType;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.v85.runtime.Runtime;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;

    private final CrawlConfig crawlConfig;


    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void crawlAllNews() {
        List<News> newsList = new ArrayList<>();
        // 모든 사이트 크롤링
        for (NewsSite newsSite : crawlConfig.getSites()) {
            // 대륙별로 크롤링하기
            for (ContinentType continentType : ContinentType.values()) {
                if (newsSite.getContinentUrl().get(continentType) != null) {
                    List<News> newsResults = crawlNews(newsSite, continentType);
                    newsList.addAll(newsResults);
                }
            }
        }

        newsRepository.saveAll(newsList);
        log.info("기사 업데이트를 완료했습니다. 업데이트된 기사 총 갯수 : {}", newsList.size());
    }

    public List<News> crawlNews(@NotNull NewsSite newsSite, ContinentType continentType) {
        WebDriverManager.chromedriver().setup();

        // 브라우저 옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // GUI 없이 실행
        options.addArguments("--disable-gpu"); // 백그라운드 실행
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        // 웹 드라이버 실행
        WebDriver driver = new ChromeDriver(options);

        List<News> newsList = new ArrayList<>();
        LocalDateTime latestArticleTime = getLatestNews(newsSite.getName(), continentType);

        try {
            String baseUrl = newsSite.getBaseUrl() + newsSite.getContinentUrl().get(continentType);

            int currentPage = 1;
            int cnt = 0;
            boolean stopFlag = false;
            while (!stopFlag) {
                String pageUrl = baseUrl + "?" + newsSite.getPageParam() + "=" + currentPage;
                log.info("현재 크롤링 중인 페이지 : " + pageUrl);
                driver.get(pageUrl);
                Thread.sleep(500);
                // 뉴스 목록 가져오기 (XPath 기반)
                List<WebElement> articles = driver.findElements(
                    By.xpath(newsSite.getArticleXpath()));
                log.info("현재 페이지의 기사 갯수 : " + articles.size());
                if (articles.isEmpty()) {
                    log.error("웹 페이지를 탐색할 수 없습니다.");
                    break;
                }
                // 기사 정보 출력
                for (WebElement article : articles) {
                    try {
                        String title = article.findElement(By.xpath(newsSite.getTitleXpath()))
                            .getText();
                        String link = article.findElement(By.xpath(newsSite.getUrlXpath()))
                            .getAttribute("href");
                        String description = article.findElement(
                            By.xpath(newsSite.getContentXpath())).getText();
                        if (description.length() > 254) {
                            description = description.substring(0, 254);
                        }
                        String imgUrl = article.findElement(By.xpath(newsSite.getImgXpath()))
                            .getAttribute("src");
                        String date = article.findElement(By.xpath(newsSite.getDateXpath()))
                            .getText().replace(".", "-");

                        // 날짜 형식 통일
                        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .appendPattern("yyyy-MM-dd HH:mm")
                            .optionalStart()
                            .appendPattern(":ss")   // 초단위가 있을수도 없을수도 있음.
                            .optionalEnd()
                            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0) // 초단위가 없다면 0으로 초기화
                            .toFormatter();

                        LocalDateTime deliveryTime = LocalDateTime.parse(date, formatter);

                        // 이미 크롤링 한 뉴스 중복 방지
                        if (deliveryTime.isBefore(latestArticleTime) || deliveryTime.equals(
                            latestArticleTime)) {
                            stopFlag = true;
                            break;
                        }

                        News news = News.builder()
                            .newsType(newsSite.getName())
                            .link(link)
                            .continent(continentType)
                            .title(title)
                            .content(description)
                            .deliveryTime(deliveryTime)
                            .imgUrl(imgUrl)
                            .build();

                        newsList.add(news);

                        // 크롤링한 뉴스 갯수
                        cnt++;

                        if (cnt >= 50) {
                            stopFlag = true;
                            break;
                        }

                    } catch (Exception e) {
                        // 예외발생은 이미지가 없는 기사의 경우가 대부분입니다.
                        // 그 외에 하나라도 빠지는 요소가 있을 경우 배제
                        log.error("크롤링 에러 : 일부 요소를 찾을 수 없습니다.");
                    }
                }

                currentPage++;


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 브라우저 종료
            driver.quit();
        }
        log.info(newsSite.getName().getValue() + "에서 총 " + newsList.size() + "개의 뉴스를 크롤링했습니다.");
        return newsList;
    }

    // 뉴스 사이트별, 대륙별로 DB에 저장된 가장 최신기사의 작성시간 확인
    // 그 이전에 작성된 기사는 크롤링 하지 않습니다. (중복 방지)
    public LocalDateTime getLatestNews(NewsType newsType, ContinentType continentType) {
        return newsRepository.latestNews(newsType, continentType);
    }
}

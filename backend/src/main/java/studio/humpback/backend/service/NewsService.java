package studio.humpback.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studio.humpback.backend.model.News;
import studio.humpback.backend.model.NewsTranslation;
import studio.humpback.backend.repository.NewsRepository;
import studio.humpback.backend.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private static final String NEWS_NOT_FOUND = "News not found";

    private final NewsRepository newsRepository;

    public News get(String id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NEWS_NOT_FOUND));
    }

    public News create(String lang, String title, String content) {
        NewsTranslation translation = NewsTranslation.builder()
                .lang(lang)
                .title(title)
                .content(content)
                .build();

        News news = News.builder()
                .translations(new ArrayList<>(List.of(translation)))
                .createdAt(Instant.now())
                .build();

        return newsRepository.save(news);
    }

    public News update(String id, String lang, String title, String content) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NEWS_NOT_FOUND));

        news.getTranslations().removeIf(t -> t.getLang().equalsIgnoreCase(lang));

        news.getTranslations()
            .add(NewsTranslation.builder()
                .lang(lang)
                .title(title)
                .content(content)
                .build());

        return newsRepository.save(news);
    }

    public Page<News> getPage(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    public void delete(String id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NEWS_NOT_FOUND));

        newsRepository.delete(news);
    }
}
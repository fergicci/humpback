package studio.humpback.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studio.humpback.backend.model.News;
import studio.humpback.backend.repository.NewsRepository;
import studio.humpback.backend.exception.ResourceNotFoundException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NewsService {

    private static final String NEWS_NOT_FOUND = "News not found";
    private final NewsRepository newsRepository;

    public News get(String id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NEWS_NOT_FOUND));
    }

    public News create(String title, String content) {
        News news = News.builder()
                .title(title)
                .content(content)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return newsRepository.save(news);
    }

    public News update(String id, String title, String content) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NEWS_NOT_FOUND));

        news.setTitle(title);
        news.setContent(content);
        news.setUpdatedAt(Instant.now());

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

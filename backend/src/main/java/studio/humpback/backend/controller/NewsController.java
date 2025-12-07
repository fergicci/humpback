package studio.humpback.backend.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.NewsRequest;
import studio.humpback.backend.dto.NewsResponse;
import studio.humpback.backend.dto.PagedResponse;
import studio.humpback.backend.exception.ResourceNotFoundException;
import studio.humpback.backend.model.News;
import studio.humpback.backend.model.NewsTranslation;
import studio.humpback.backend.service.NewsService;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

        private final NewsService newsService;

        @GetMapping
        public ApiResponse<PagedResponse<NewsResponse>> getAllNews(
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Locale locale) {

                Pageable pageable = PageRequest.of(
                                page - 1,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<News> newsPage = newsService.getPage(pageable);

                List<NewsResponse> content = newsPage.getContent()
                                .stream()
                                .map(news -> toResponse(news, locale.getLanguage()))
                                .toList();

                PagedResponse<NewsResponse> pagedResponse = PagedResponse.<NewsResponse>builder()
                                .content(content)
                                .page(newsPage.getNumber())
                                .size(newsPage.getSize())
                                .totalElements(newsPage.getTotalElements())
                                .totalPages(newsPage.getTotalPages())
                                .build();

                return ApiResponse.success(pagedResponse);
        }

        private NewsResponse toResponse(News news, String lang) {
                NewsTranslation translation = news.getTranslations().stream()
                                .filter(t -> t.getLang().equalsIgnoreCase(lang))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Translation not found"));

                return NewsResponse.builder()
                                .id(news.getId())
                                .title(translation.getTitle())
                                .content(translation.getContent())
                                .createdAt(news.getCreatedAt())
                                .build();
        }

        @PostMapping
        @PreAuthorize("hasAuthority('ADMIN')")
        public ApiResponse<NewsResponse> createNews(
                        @RequestBody @Valid NewsRequest newsRequest,
                        Locale locale) {
                String lang = locale.getLanguage();
                News news = newsService.create(lang, newsRequest.getTitle(), newsRequest.getContent());
                return ApiResponse.success(toResponse(news, lang));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ADMIN')")
        public ApiResponse<NewsResponse> updateNews(
                        @PathVariable String id,
                        @RequestBody @Valid NewsRequest newsRequest,
                        Locale locale) {
                String lang = locale.getLanguage();
                News news = newsService.update(id, lang, newsRequest.getTitle(), newsRequest.getContent());
                return ApiResponse.success(toResponse(news, lang));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ADMIN')")
        public ApiResponse<Void> deleteNews(@PathVariable String id) {
                newsService.delete(id);
                return ApiResponse.success();
        }
}

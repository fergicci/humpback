package studio.humpback.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import studio.humpback.backend.model.News;

public interface NewsRepository extends MongoRepository<News, String> {
}

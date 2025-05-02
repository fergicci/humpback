package studio.humpback.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "news")
public class News {

    @Id
    private String id;
    private List<NewsTranslation> translations;
    private Instant createdAt;
    private Instant updatedAt;
}

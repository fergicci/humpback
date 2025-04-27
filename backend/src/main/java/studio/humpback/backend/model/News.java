package studio.humpback.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "news")
public class News {

    @Id
    private String id;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
}

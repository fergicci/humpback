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
@Document(collection = "contacts")
public class Contact {

    @Id
    private String id;
    private String name;
    private String email;
    private String telephone;
    private String message;
    private Instant createdAt;
    
    @Builder.Default
    private Boolean read = false;
}

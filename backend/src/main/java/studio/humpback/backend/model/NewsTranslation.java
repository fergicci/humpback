package studio.humpback.backend.model;

import lombok.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsTranslation {

    private String lang;
    private String title;
    private String content;
}
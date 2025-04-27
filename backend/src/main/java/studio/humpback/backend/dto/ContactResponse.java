package studio.humpback.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ContactResponse {

    private String id;
    private String name;
    private String email;
    private String telephone;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

}
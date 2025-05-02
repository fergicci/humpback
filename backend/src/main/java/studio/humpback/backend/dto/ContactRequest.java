package studio.humpback.backend.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class ContactRequest {

    @NotBlank(message = "{contact.request.name.required}")
    private String name;

    @NotBlank(message = "{contact.request.email.required}")
    @Email(message = "{contact.request.email.invalid}")
    private String email;

    @NotBlank(message = "{contact.request.message.required}")
    @Size(max = 500, message = "{contact.request.message.size}")
    private String message;

}

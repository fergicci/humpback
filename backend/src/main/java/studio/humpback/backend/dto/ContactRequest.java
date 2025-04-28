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

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Telephone is required")
    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\./0-9]*$", message = "Invalid telephone format")
    private String telephone;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must be at most 500 characters")
    private String message;

}
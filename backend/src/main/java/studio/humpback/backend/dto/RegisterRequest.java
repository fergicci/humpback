package studio.humpback.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "{register.request.username.required}")
    private String username;

    @NotBlank(message = "{register.request.fullname.required}")
    private String fullname;

    @NotBlank(message = "{register.request.email.required}")
    @Email(message = "{register.request.email.invalid}")
    private String email;

    @NotBlank(message = "{register.request.password.required}")
    @Size(min = 8, max = 30, message = "{register.request.password.size}")
    private String password;

}

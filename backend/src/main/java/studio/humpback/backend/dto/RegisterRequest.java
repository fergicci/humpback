package studio.humpback.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "{register.request.username.required}")
    @Size(min = 3, max = 32, message = "{register.request.username.size}")
    @Pattern(regexp = "^[a-z0-9][a-z0-9._-]{2,31}$", message = "{register.request.username.pattern}")
    private String username;

    @NotBlank(message = "{register.request.fullname.required}")
    private String fullname;

    @NotBlank(message = "{register.request.email.required}")
    @Email(message = "{register.request.email.invalid}")
    private String email;

    @NotBlank(message = "{register.request.password.required}")
    @Size(min = 12, max = 64, message = "{register.request.password.size}")
    @Pattern(
            regexp = "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,64}$",
            message = "{register.request.password.pattern}")
    private String password;

}

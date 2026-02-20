package studio.humpback.backend.dto;

import java.util.Date;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "{user.request.fullname.required}")
    private String fullname;
    
    @NotBlank(message = "{user.request.email.required}")
    @Email(message = "{user.request.email.invalid}")
    private String email;
    
    @NotNull(message = "{user.request.passwordExpiredAt.required}")
    @Future(message = "{user.request.passwordExpiredAt.future}")
    private Date passwordExpiredAt;
    
    private boolean disabled;
    private boolean accountLocked;
    
    @NotEmpty(message = "{user.request.roles.required}")
    private Set<String> roles;

}

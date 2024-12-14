package danix.app.Store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthDTO {
    @Email(message = "email must be correct")
    private String email;

    @NotEmpty(message = "password must not be empty")
    @Size(min = 5, max = 30, message = "Password must be between 5 and 30 characters.")
    private String password;
}

package danix.app.Store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationUserDTO {
    @NotEmpty(message = "Username cant be empty.")
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters.")
    private String username;

    @Email(message = "email must be correct")
    @NotEmpty(message = "email must not be empty")
    private String email;

    @NotEmpty(message = "password must not be empty")
    @Size(min = 5, max = 30, message = "Password must be between 5 and 30 characters.")
    private String password;
}

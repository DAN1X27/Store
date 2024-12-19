package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoverPasswordDTO implements RequestEmailKey {

    @NotEmpty(message = "Email must not be empty")
    private String email;

    @NotEmpty(message = "Password must not be empty")
    @Size(min = 5, max = 30, message = "New password must be between 5 and 30 characters.")
    private String newPassword;

    @NotNull(message = "Key must not be empty")
    private Integer key;
}

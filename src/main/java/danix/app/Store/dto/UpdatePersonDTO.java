package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePersonDTO {
    @NotEmpty(message = "Old password must not be empty")
    private String oldPassword;

    @NotEmpty(message = "New password must not be empty")
    @Size(min = 5, max = 30, message = "New password must be between 5 and 30 characters.")
    private String newPassword;
}

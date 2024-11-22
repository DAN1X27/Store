package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class RecoverPasswordDTO {

    @NotEmpty(message = "Email must not be empty")
    private String email;

    @NotEmpty(message = "Password must not be empty")
    @Size(min = 5, max = 30, message = "New password must be between 5 and 30 characters.")
    private String newPassword;

    public @NotEmpty(message = "Email must not be empty") String getEmail() {
        return email;
    }

    public void setEmail(@NotEmpty(message = "Email must not be empty") String email) {
        this.email = email;
    }

    public @NotEmpty(message = "Password must not be empty") String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(@NotEmpty(message = "Password must not be empty") String newPassword) {
        this.newPassword = newPassword;
    }
}

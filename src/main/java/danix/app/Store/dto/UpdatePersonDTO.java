package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class UpdatePersonDTO {
    @NotEmpty(message = "Old password must not be empty")
    private String oldPassword;

    @NotEmpty(message = "New password must not be empty")
    @Size(min = 5, max = 30, message = "New password must be between 5 and 30 characters.")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

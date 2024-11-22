package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;

public class BanUserDTO {

    @NotEmpty(message = "Username must not bw empty")
    private String username;

    @NotEmpty(message = "Reason must not be empty")
    private String reason;

    public @NotEmpty(message = "Username must not bw empty") String getUsername() {
        return username;
    }

    public void setUsername(@NotEmpty(message = "Username must not bw empty") String username) {
        this.username = username;
    }

    public @NotEmpty(message = "Reason must not be empty") String getReason() {
        return reason;
    }

    public void setReason(@NotEmpty(message = "Reason must not be empty") String reason) {
        this.reason = reason;
    }
}

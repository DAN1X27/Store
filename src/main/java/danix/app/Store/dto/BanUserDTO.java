package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BanUserDTO {

    @NotEmpty(message = "Username must not bw empty")
    private String username;

    @NotEmpty(message = "Reason must not be empty")
    private String reason;
}

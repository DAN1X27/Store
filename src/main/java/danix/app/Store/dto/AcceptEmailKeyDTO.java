package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptEmailKeyDTO {
    @NotEmpty(message = "Email cannot be empty")
    private String email;
    @NotNull(message = "Key cannot be empty")
    private int key;
}

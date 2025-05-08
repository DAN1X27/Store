package danix.app.Store.dto;

import danix.app.Store.models.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ResponseUserDTO {
    private String username;
    private String email;
    private List<ResponseAdminOrderDTO> orders;
    private LocalDateTime createdAt;
    private User.Roles role;
    private User.Status status;

}

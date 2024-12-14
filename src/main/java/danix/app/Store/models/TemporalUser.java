package danix.app.Store.models;

import danix.app.Store.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TemporalUser {
    private UserDTO user;
    private Date expiredTime;
}

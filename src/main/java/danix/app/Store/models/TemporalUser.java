package danix.app.Store.models;

import danix.app.Store.dto.RegistrationUserDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TemporalUser {
    private RegistrationUserDTO user;
    private Date expiredTime;
}

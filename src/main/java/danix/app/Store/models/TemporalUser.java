package danix.app.Store.models;

import danix.app.Store.dto.PersonDTO;

import java.util.Date;

public class TemporalUser {
    private PersonDTO user;
    private Date expiredTime;

    public TemporalUser(PersonDTO user, Date expiredTime) {
        this.user = user;
        this.expiredTime = expiredTime;
    }

    public PersonDTO getUser() {
        return user;
    }

    public void setUser(PersonDTO user) {
        this.user = user;
    }

    public Date getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }
}

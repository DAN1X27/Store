package danix.app.Store.models;

import java.util.Date;

public class EmailKeys {
    private int key;
    private Date expiredAt;

    public EmailKeys(int key, Date expiredAt) {
        this.key = key;
        this.expiredAt = expiredAt;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Date getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Date expiredAt) {
        this.expiredAt = expiredAt;
    }
}

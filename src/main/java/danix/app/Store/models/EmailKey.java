package danix.app.Store.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_keys")
@Getter
@Setter
@NoArgsConstructor
public class EmailKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "key")
    private Integer key;
    @Column(name = "expired_time")
    private LocalDateTime expiredTime;

    private Integer attempts;

    private String email;

    public EmailKey(int key, LocalDateTime expiredTime) {
        this.key = key;
        this.expiredTime = expiredTime;
    }
}

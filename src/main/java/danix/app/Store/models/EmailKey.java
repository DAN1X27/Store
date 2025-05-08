package danix.app.Store.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_keys")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
}

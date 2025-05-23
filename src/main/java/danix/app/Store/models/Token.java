package danix.app.Store.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "Tokens")
@Getter
@Setter
public class Token {
    @Id
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "expired_at")
    private Date expiredDate;
}

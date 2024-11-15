package danix.app.Store.models;

import jakarta.persistence.*;
import org.hibernate.engine.spi.Status;

@Entity
@Table(name = "Tokens")
public class Token {
    @Id
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Person owner;

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }
}

package danix.app.Store.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Person")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    @NotEmpty(message = "Username cant be empty.")
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters.")
    private String username;

    @Column(name = "email")
    @Email(message = "email must be correct")
    private String email;

    @Column(name = "password")
    @NotEmpty(message = "password must not be empty")
    private String password;

    @OneToMany(mappedBy = "owner")
    private List<Order> orders;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner")
    private List<Token> token;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Roles role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    public enum Roles {
        ROLE_USER,
        ROLE_ADMIN
    }

    public enum Status {
        REGISTERED,
        TEMPORAL_REGISTERED,
        BANNED
    }
}

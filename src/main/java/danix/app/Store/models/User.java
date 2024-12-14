package danix.app.Store.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Person")
@Data
@NoArgsConstructor
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

    public static Builder builder() {
        return new Builder();
    }

    public User(Builder builder) {
        this.username = builder.username;
        this.email = builder.email;
        this.password = builder.password;
        this.role = builder.role;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    public enum Roles {
        ROLE_USER,
        ROLE_ADMIN
    }

    public enum Status {
        REGISTERED,
        TEMPORAL_REGISTERED,
        BANNED
    }

    public static class Builder {
        private String username;
        private String email;
        private String password;
        private LocalDateTime createdAt;
        private Roles role;
        private Status status;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder role(Roles role) {
            this.role = role;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}

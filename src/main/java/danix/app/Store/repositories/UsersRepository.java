package danix.app.Store.repositories;

import danix.app.Store.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Modifying
    void deleteAllByStatusAndCreatedAtBefore(User.Status status, LocalDateTime createdAt);
}

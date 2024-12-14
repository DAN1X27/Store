package danix.app.Store.repositories;

import danix.app.Store.models.EmailKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailKeysRepository extends JpaRepository<EmailKey, Integer> {
    Optional<EmailKey> findByEmail(String email);

    @Modifying
    @Query("delete from EmailKey where expiredTime <= current_timestamp")
    void deleteExpiredEmailKeys();
}

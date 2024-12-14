package danix.app.Store.repositories;

import danix.app.Store.models.User;
import danix.app.Store.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokensRepository extends JpaRepository<Token, String> {
   List<Token> findAllByOwner(User owner);

   @Modifying
   @Query("delete from Token where expiredDate <= current_date or status='REVOKED'")
   void deleteExpiredTokens();
}

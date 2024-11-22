package danix.app.Store.repositories;

import danix.app.Store.models.Person;
import danix.app.Store.models.Token;
import danix.app.Store.models.TokenStatus;
import org.modelmapper.spi.Tokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokensRepository extends JpaRepository<Token, String> {
   List<Token> findAllByOwner(Person owner);
}

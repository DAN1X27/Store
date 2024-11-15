package danix.app.Store.repositories;

import danix.app.Store.models.Person;
import danix.app.Store.models.Token;
import org.modelmapper.spi.Tokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokensRepository extends JpaRepository<Token, String> {
   List<Token> findAllByOwner(Person owner);
}

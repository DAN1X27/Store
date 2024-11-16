package danix.app.Store.repositories;

import danix.app.Store.models.Cart;
import danix.app.Store.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByOwner(Person owner);
}

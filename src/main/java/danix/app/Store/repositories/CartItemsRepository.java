package danix.app.Store.repositories;

import danix.app.Store.models.Cart;
import danix.app.Store.models.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItems, Integer> {
    List<CartItems> findByCart(Cart cart);
}

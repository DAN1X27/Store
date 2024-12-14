package danix.app.Store.services;

import danix.app.Store.models.Cart;
import danix.app.Store.models.CartItems;
import danix.app.Store.repositories.CartItemsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartItemsService {
    private final CartItemsRepository cartItemsRepository;

    public List<CartItems> getByCart(Cart cart) {
        return cartItemsRepository.findByCart(cart);
    }

    @Transactional
    public void save(CartItems cartItems) {
        cartItemsRepository.save(cartItems);
    }
}

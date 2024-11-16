package danix.app.Store.services;

import danix.app.Store.models.Cart;
import danix.app.Store.models.CartItems;
import danix.app.Store.repositories.CartItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CartItemsService {
    private final CartItemsRepository cartItemsRepository;

    @Autowired
    public CartItemsService(CartItemsRepository cartItemsRepository) {
        this.cartItemsRepository = cartItemsRepository;
    }

    public List<CartItems> getByCart(Cart cart) {
        return cartItemsRepository.findByCart(cart);
    }

    @Transactional
    public void save(CartItems cartItems) {
        cartItemsRepository.save(cartItems);
    }

    @Transactional
    public void delete(CartItems cartItems) {
        cartItemsRepository.delete(cartItems);
    }
}

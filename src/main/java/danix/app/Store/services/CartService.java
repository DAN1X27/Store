package danix.app.Store.services;

import danix.app.Store.dto.*;
import danix.app.Store.models.Cart;
import danix.app.Store.models.CartItems;
import danix.app.Store.models.Item;
import danix.app.Store.models.Person;
import danix.app.Store.repositories.CartRepository;
import danix.app.Store.util.CartException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;
    private final ItemService itemService;
    private final CartItemsService cartItemsService;
    private final OrderService orderService;

    @Autowired
    public CartService(CartRepository cartRepository, ModelMapper modelMapper, ItemService itemService,
                       CartItemsService cartItemsService, OrderService orderService) {
        this.cartRepository = cartRepository;
        this.modelMapper = modelMapper;
        this.itemService = itemService;
        this.cartItemsService = cartItemsService;
        this.orderService = orderService;
    }

    public ResponseCartDTO getByOwner(Person owner) {
        return convertToCartDTO(cartRepository.findByOwner(owner).orElseThrow(() -> new CartException("Cart is empty")));
    }

    @Transactional
    public void addCart(CartDTO cartDTO) {
        cartRepository.save(convertToCart(cartDTO));
    }

    @Transactional
    public void cleanCart() {
        Person owner = PersonService.getCurrentUser();
        cartRepository.delete(cartRepository.findByOwner(owner).orElseThrow(() -> new CartException("Cart is empty")));
    }

    @Transactional
    public void createOrder() {
        Person owner = PersonService.getCurrentUser();
        Cart cart = cartRepository.findByOwner(owner).orElseThrow(() -> new CartException("Cart is empty"));
        List<CartItems> cartItems = cartItemsService.getByCart(cart);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setItems(cart.getItems().stream().map(item -> modelMapper.map(item, ItemDTO.class)).toList());
        for (CartItems cartItem : cartItems) {
            for (ItemDTO itemDTO : orderDTO.getItems()) {
                if(cartItem.getItem().getName().equals(itemDTO.getName())) {
                    itemDTO.setCount(cartItem.getCount());
                }
            }
        }
        orderService.createOrder(orderDTO);
    }

    private Cart convertToCart(CartDTO cartDTO) {
        Person owner = PersonService.getCurrentUser();
        Optional<Cart> availableCart = cartRepository.findByOwner(owner);

        if(availableCart.isEmpty()) {

            Cart cart = new Cart();
            cart.setItems(cartDTO.getItems().stream().map(item ->
                    itemService.findItemByName(item.getName()).get()).toList());

            cart.setOwner(owner);

            double sum = 0;

            for (Item item : cart.getItems()) {
                for (ItemDTO itemDTO : cartDTO.getItems()) {
                    if (item.getName().equals(itemDTO.getName())) {
                        CartItems cartItems = new CartItems(cart, item, itemDTO.getCount());

                        cartItemsService.save(cartItems);
                        sum += item.getPrice() * itemDTO.getCount();
                        break;
                    }
                }
            }
            cart.setPrice(sum);

            return cart;
        }else {
            List<CartItems> cartItems = cartItemsService.getByCart(availableCart.get());
            double sum = 0;
            for (ItemDTO itemDTO : cartDTO.getItems()) {

                for (Item i : availableCart.get().getItems()) {
                    if (i.getName().equals(itemDTO.getName())) {
                        for (CartItems cartItem : cartItems) {
                            if(cartItem.getItem().getName().equals(itemDTO.getName())) {
                                cartItem.setCount(cartItem.getCount() + itemDTO.getCount());
                                sum+= itemService.findItemByName(i.getName()).get().getPrice() * cartItem.getCount();
                                break;
                            }
                        }
                    }
                }
            }
            availableCart.get().setPrice(sum);
            return availableCart.get();
        }
    }

    private ResponseCartDTO convertToCartDTO(Cart cart) {
        ResponseCartDTO cartDTO = new ResponseCartDTO();
        cartDTO.setItems(cart.getItems().stream().map(item -> modelMapper.map(item, SaveItemDTO.class)).toList());

        List<CartItems> cartItems = cartItemsService.getByCart(cart);

        for (SaveItemDTO saveItemDTO : cartDTO.getItems()) {
            for (CartItems cartItem : cartItems) {

                if (saveItemDTO.getName().equals(cartItem.getItem().getName())) {
                    double price = saveItemDTO.getPrice() * cartItem.getCount();
                    saveItemDTO.setPrice(price);
                    saveItemDTO.setCount(cartItem.getCount());
                    break;
                }
            }
        }
        cartDTO.setPrice(cart.getPrice());
        return cartDTO;
    }
}

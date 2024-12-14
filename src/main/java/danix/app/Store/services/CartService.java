package danix.app.Store.services;

import danix.app.Store.dto.*;
import danix.app.Store.models.Cart;
import danix.app.Store.models.CartItems;
import danix.app.Store.models.Item;
import danix.app.Store.models.User;
import danix.app.Store.repositories.CartRepository;
import danix.app.Store.util.CartException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;
    private final ItemService itemService;
    private final CartItemsService cartItemsService;
    private final OrderService orderService;

    public ResponseCartDTO getByOwner(User owner) {
        return convertToCartDTO(cartRepository.findByOwner(owner).orElseThrow(() -> new CartException("Cart is empty")));
    }

    @Transactional
    public void addCart(CartDTO cartDTO) {
        cartRepository.save(convertToCart(cartDTO));
    }

    @Transactional
    public void cleanCart() {
        User owner = UserService.getCurrentUser();
        cartRepository.delete(cartRepository.findByOwner(owner).orElseThrow(() -> new CartException("Cart is empty")));
    }

    @Transactional
    public void createOrder() {
        User owner = UserService.getCurrentUser();
        Cart cart = cartRepository.findByOwner(owner).orElseThrow(() -> new CartException("Cart is empty"));
        Map<String, CartItems> cartItemsMap = getCartItems(cart);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setItems(cart.getItems().stream().map(item -> modelMapper.map(item, ItemDTO.class)).toList());
        for (ItemDTO itemDTO : orderDTO.getItems()) {
            CartItems cartItem = cartItemsMap.get(itemDTO.getName());
            if (cartItem != null) {
                itemDTO.setCount(cartItem.getCount());
            }
        }
        orderService.createOrder(orderDTO);
    }

    private Cart convertToCart(CartDTO cartDTO) {
        User owner = UserService.getCurrentUser();
        Optional<Cart> availableCart = cartRepository.findByOwner(owner);
        Cart cart;
        if (availableCart.isEmpty()) {
            cart = new Cart();
            cart.setOwner(owner);
            cart.setItems(getItems(cart, cartDTO));
            cart.setPrice(cartDTO.getItems().stream()
                    .mapToDouble(item -> item.getCount() * itemService.getItemByName(item.getName()).getPrice()).sum());
        } else {
            cart = availableCart.get();
            double price = updateCartItems(cartDTO, cart);
            cart.setPrice(cart.getPrice() + price);
        }
        return cart;
    }
    
    private double updateCartItems(CartDTO cartDTO, Cart cart) {
        Map<String, CartItems> cartItemsMap = getCartItems(cart);
        double sum = 0;
        for (ItemDTO itemDTO : cartDTO.getItems()) {
            CartItems cartItem = cartItemsMap.get(itemDTO.getName());
            if (cartItem == null) {
                Item item = itemService.getItemByName(itemDTO.getName());
                cartItem = new CartItems(cart, item , itemDTO.getCount());
                cartItemsService.save(cartItem);
                cart.getItems().add(item);
            } else {
                cartItem.setCount(cartItem.getCount() + itemDTO.getCount());
            }
            sum += cartItem.getItemPrice() * itemDTO.getCount();
        }
        return sum;
    }

    private SaveItemDTO setPrice(SaveItemDTO itemDTO, Map<String, CartItems> cartItemsMap) {
        CartItems cartItem = cartItemsMap.get(itemDTO.getName());
        if (cartItem != null) {
            itemDTO.setCount(cartItem.getCount());
            itemDTO.setPrice(itemDTO.getPrice() * cartItem.getCount());
        }
        return itemDTO;
    }

    private ResponseCartDTO convertToCartDTO(Cart cart) {
        ResponseCartDTO cartDTO = new ResponseCartDTO();
        Map<String, CartItems> cartItemsMap = getCartItems(cart);
        cartDTO.setItems(cart.getItems().stream()
                .map(item -> modelMapper.map(item, SaveItemDTO.class))
                .map(item -> setPrice(item, cartItemsMap))
                .toList());
        cartDTO.setPrice(cart.getPrice());
        return cartDTO;
    }

    private List<Item> getItems(Cart cart, CartDTO cartDTO) {
        Map<String, ItemDTO> items = cartDTO.getItems().stream()
                .collect(Collectors.toMap(ItemDTO::getName, Function.identity()));
        return cartDTO.getItems().stream()
                .map(item -> itemService.getItemByName(item.getName()))
                .map(item -> createCartItems(items, item, cart))
                .collect(Collectors.toList());
    }

    private Item createCartItems(Map<String, ItemDTO> itemDTOs, Item item, Cart cart) {
        ItemDTO itemDTO = itemDTOs.get(item.getName());
        if (itemDTO != null) {
            CartItems cartItems = new CartItems(cart, item, itemDTO.getCount());
            cartItemsService.save(cartItems);
        }
        return item;
    }

    private Map<String, CartItems> getCartItems(Cart cart) {
        List<CartItems> cartItems = cartItemsService.getByCart(cart);
        return cartItems.stream()
                .collect(Collectors.toMap(CartItems::getItemName, Function.identity()));
    }
}

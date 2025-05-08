package danix.app.Store.controllers;

import danix.app.Store.dto.CartDTO;
import danix.app.Store.dto.ResponseCartDTO;
import danix.app.Store.models.User;
import danix.app.Store.services.CartService;
import danix.app.Store.services.UserService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartsController {
    private final CartService cartService;
    private final CartValidator cartValidator;

    @GetMapping
    public ResponseEntity<ResponseCartDTO> showUserCart() {
        User currentUser = UserService.getCurrentUser();
        return new ResponseEntity<>(cartService.getByOwner(currentUser), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> createUserCart(@RequestBody @Valid CartDTO cartDTO, BindingResult bindingResult) {
        cartValidator.validate(cartDTO, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.CART_EXCEPTION);

        cartService.addCart(cartDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/order")
    public ResponseEntity<HttpStatus> createOrder() {
        cartService.createOrder();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/clean")
    public ResponseEntity<HttpStatus> cleanUserCart() {
        cartService.cleanCart();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(CartException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

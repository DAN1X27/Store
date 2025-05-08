package danix.app.Store.controllers;

import danix.app.Store.dto.*;
import danix.app.Store.services.OrderService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {
    private final OrderService orderService;
    private final OrderValidator orderValidator;

    @DeleteMapping("/{id}/take")
    public ResponseEntity<HttpStatus> takeOrder(@PathVariable("id") int id) {
        orderService.takeOrder(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<HttpStatus> cancelOrder(@PathVariable("id") int id) {
        orderService.cancelOrder(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public List<ResponseOrderDTO> getUserOrders(@RequestParam int page, @RequestParam int count) {
        return orderService.getAllUserOrders(page, count);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public List<ResponseAdminOrderDTO> getAll(@RequestParam int page, @RequestParam int count) {
        return orderService.getAllOrders(page, count);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid OrderDTO orderDTO,
                                             BindingResult bindingResult) {
        orderValidator.validate(orderDTO, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.ORDER_EXCEPTION);
        orderService.createOrder(orderDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(OrderException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}

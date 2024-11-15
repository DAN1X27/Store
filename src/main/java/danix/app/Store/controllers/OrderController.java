package danix.app.Store.controllers;

import danix.app.Store.dto.*;
import danix.app.Store.services.OrderService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderValidator orderValidator;

    @Autowired
    public OrderController(OrderService orderService, OrderValidator orderValidator) {
        this.orderService = orderService;
        this.orderValidator = orderValidator;
    }

    @DeleteMapping("/takeOrder/{id}")
    public ResponseEntity<HttpStatus> takeOrder(@PathVariable("id") int id) {
        orderService.takeOrder(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @DeleteMapping("/cancelOrder/{id}")
    public ResponseEntity<HttpStatus> cancelOrder(@PathVariable("id") int id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping
    public List<ResponseOrderDTO> getUserOrders() {
        return orderService.getAllUserOrders();
    }

    @GetMapping("/getAll")
    public List<AdminOrderDTO> getAll() {

        return orderService.getAllOrders();
    }

    @PostMapping("/create")
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid OrderDTO orderDTO,
                                             BindingResult bindingResult) {
        orderValidator.validate(orderDTO, bindingResult);
        OrderErrorHandler.exceptionHandle(bindingResult);

        orderService.createOrder(orderDTO);

        return ResponseEntity.ok(HttpStatus.OK);
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

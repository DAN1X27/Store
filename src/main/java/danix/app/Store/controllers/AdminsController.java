package danix.app.Store.controllers;

import danix.app.Store.dto.ResponseAdminOrderDTO;
import danix.app.Store.dto.BanUserDTO;
import danix.app.Store.dto.ResponseUserDTO;
import danix.app.Store.models.User;
import danix.app.Store.services.AdminService;
import danix.app.Store.services.OrderService;
import danix.app.Store.services.UserService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminsController {
    private final AdminService adminService;
    private final OrderService orderService;
    private final UserService userService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/orders")
    public List<ResponseAdminOrderDTO> getAllOrders(@RequestParam int page, @RequestParam int count) {
        return orderService.getAllOrders(page, count);
    }

    @GetMapping("/orders/user")
    public List<ResponseAdminOrderDTO> getAllUserOrders(@RequestBody Map<String, String> user, @RequestParam int page,
                                                        @RequestParam int count) {
        requestHelper(user);
        return orderService.getAllUserOrdersForAdmin(user.get("username"), page, count);
    }

    @DeleteMapping("/order/{id}/cancel")
    public ResponseEntity<HttpStatus> cancelUserOrder(@PathVariable("id") int id) {
        orderService.cancelOrderByAdmin(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/users")
    public List<ResponseUserDTO> getAllUsers(@RequestParam int page, @RequestParam int count) {
        return adminService.getAllUsers(page, count);
    }

    @PostMapping("/user/find")
    public ResponseUserDTO findUser(@RequestBody Map<String, String> user) {
        requestHelper(user);
        return adminService.findUserByUsername(user.get("username")).get();
    }

    @PatchMapping("/user/ban")
    public ResponseEntity<HttpStatus> banUser(@RequestBody @Valid BanUserDTO banUserDTO,
                                          BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.USER_EXCEPTION);
        User user = userService.getUserByUserName(banUserDTO.getUsername())
                        .orElseThrow(() -> new UserException("User not found"));
        adminService.banUser(user);
        kafkaTemplate.send("banUser-topic", user.getEmail(), banUserDTO.getReason());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/unbanUser")
    public ResponseEntity<HttpStatus> unbanUser(@RequestBody Map<String, String> user) {
        User person = requestHelper(user);
        adminService.unbanUser(person);
        kafkaTemplate.send("unbanUser-topic", person.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(OrderException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(UserException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private User requestHelper(Map<String, String> user) {

        if(!user.containsKey("username")) {
            throw new UserException("Incorrect key");
        }
        if(user.get("username").isBlank()) {
            throw new UserException("Username must be not empty");
        }
        return userService.getUserByUserName(user.get("username")).orElseThrow(() -> new UserException("User not found"));
    }

}

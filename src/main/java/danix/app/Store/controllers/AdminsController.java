package danix.app.Store.controllers;

import danix.app.Store.dto.AdminOrderDTO;
import danix.app.Store.dto.ResponsePersonDTO;
import danix.app.Store.models.Person;
import danix.app.Store.services.AdminService;
import danix.app.Store.services.OrderService;
import danix.app.Store.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminsController {
    private final AdminService adminService;
    private final OrderService orderService;

    @Autowired
    public AdminsController(AdminService adminService, OrderService orderService) {
        this.adminService = adminService;
        this.orderService = orderService;
    }

    @GetMapping("/getUserOrders")
    public List<AdminOrderDTO> getAllUserOrders(@RequestBody Map<String, String> user) {
        requestHelper(user);

        return orderService.getAllUserOrdersForAdmin(user.get("username"));
    }

    @DeleteMapping("/cancelOrder/{id}")
    public ResponseEntity<HttpStatus> cancelUserOrder(@PathVariable("id") int id) {
        orderService.cancelOrderForAdmin(id);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/getAllUsers")
    public List<ResponsePersonDTO> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PostMapping("/findUser")
    public ResponsePersonDTO findUser(@RequestBody Map<String, String> user) {
        requestHelper(user);
        return adminService.findPersonByUsername(user.get("username")).get();
    }

    @PatchMapping("/banUser")
    public ResponseEntity<String> banUser(@RequestBody Map<String, String> user) {
        requestHelper(user);
        adminService.banUser(user.get("username"));
        return ResponseEntity.ok("Banned successfully: " + user.get("username"));
    }

    @PatchMapping("/unbanUser")
    public ResponseEntity<String> unbanUser(@RequestBody Map<String, String> user) {

        requestHelper(user);
        adminService.unbanUser(user.get("username"));

        return ResponseEntity.ok("Unbanned successfully: " + user.get("username"));
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

    private void requestHelper(Map<String, String> user) {

        if(!user.containsKey("username")) {
            throw new UserException("Incorrect key");
        }

        if(user.get("username").isBlank()) {
            throw new UserException("Username must be not empty");
        }

        if(adminService.findPersonByUsername(user.get("username")).isEmpty()) {
            throw new UserException("User not found");
        }
    }

}

package danix.app.Store.controllers;

import danix.app.Store.dto.AdminOrderDTO;
import danix.app.Store.dto.BanUserDTO;
import danix.app.Store.dto.ResponsePersonDTO;
import danix.app.Store.models.Person;
import danix.app.Store.services.AdminService;
import danix.app.Store.services.OrderService;
import danix.app.Store.services.PersonService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminsController {
    private final AdminService adminService;
    private final OrderService orderService;
    private final PersonService personService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public AdminsController(AdminService adminService, OrderService orderService,
                            PersonService personService, KafkaTemplate<String, String> kafkaTemplate) {
        this.adminService = adminService;
        this.orderService = orderService;
        this.personService = personService;
        this.kafkaTemplate = kafkaTemplate;
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
    public ResponseEntity<String> banUser(@RequestBody @Valid BanUserDTO banUserDTO,
                                          BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.USER_EXCEPTION);
        Person user = personService.getUserByUserName(banUserDTO.getUsername())
                        .orElseThrow(() -> new UserException("User not found"));
        adminService.banUser(user);
        kafkaTemplate.send("banUser-topic", user.getEmail(), banUserDTO.getReason());
        return ResponseEntity.ok("Banned successfully: " + banUserDTO.getUsername());
    }

    @PatchMapping("/unbanUser")
    public ResponseEntity<String> unbanUser(@RequestBody Map<String, String> user) {
        Person person = requestHelper(user);
        adminService.unbanUser(person);
        kafkaTemplate.send("unbanUser-topic", person.getEmail());
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

    private Person requestHelper(Map<String, String> user) {

        if(!user.containsKey("username")) {
            throw new UserException("Incorrect key");
        }

        if(user.get("username").isBlank()) {
            throw new UserException("Username must be not empty");
        }

        return personService.getUserByUserName(user.get("username")).orElseThrow(() -> new UserException("User not found"));
    }

}

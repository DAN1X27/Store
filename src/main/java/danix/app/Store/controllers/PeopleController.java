package danix.app.Store.controllers;

import danix.app.Store.dto.UpdatePersonDTO;
import danix.app.Store.models.Person;
import danix.app.Store.services.OrderService;
import danix.app.Store.services.PersonService;
import danix.app.Store.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class PeopleController {

    private final OrderService orderService;

    private final PasswordEncoder passwordEncoder;
    private final PersonService personService;
    private final UpdatePasswordValidator personValidator;

    @Autowired
    public PeopleController(OrderService orderService, PasswordEncoder passwordEncoder,
                            PersonService personService,
                            UpdatePasswordValidator personValidator) {
        this.orderService = orderService;
        this.passwordEncoder = passwordEncoder;
        this.personService = personService;
        this.personValidator = personValidator;
    }

    @GetMapping("/showUserInfo")
    public ResponseEntity<Map<String, Object>> showUserInfo() {

        Person person = PersonService.getCurrentUser();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", person.getUserName());
        userInfo.put("email", person.getEmail());
        userInfo.put("orders", orderService.getAllUserOrders());


        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }

    @PatchMapping("/updatePassword")
    public ResponseEntity<HttpStatus> updatePassword(@RequestBody UpdatePersonDTO updatePersonDTO,
                                                     BindingResult bindingResult) {
        Person currentUser = PersonService.getCurrentUser();
        personValidator.validate(updatePersonDTO, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.USER_EXCEPTION);

        currentUser.setPassword(passwordEncoder.encode(updatePersonDTO.getNewPassword()));
        personService.updateUser(currentUser.getId(), currentUser);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PatchMapping("/updateUsername")
    public ResponseEntity<HttpStatus> updateUsername(@RequestBody Map<String, String> newUsername) {
        Person user = PersonService.getCurrentUser();

        String username = newUsername.get("username");

        if(username == null) {
            throw new UserException("Incorrect key");

        } else if (username.isBlank()) {
            throw new UserException("New username must not be empty");

        } else if (username.length() < 2 || username.length() > 20) {
            throw new UserException("New username must be between 2 and 20 characters");

        } else if (personService.getUserByUserName(username).isPresent() && !username.equals(user.getUserName())) {
            throw new UserException("Username taken");

        } else if (username.equals(user.getUserName())) {
            throw new UserException("New username must be different from the old one");
        }

        user.setUserName(username);
        personService.updateUser(user.getId(), user);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(UserException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


}

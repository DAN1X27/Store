package danix.app.Store.controllers;

import danix.app.Store.dto.UpdatePersonDTO;
import danix.app.Store.models.User;
import danix.app.Store.services.OrderService;
import danix.app.Store.services.UserService;
import danix.app.Store.util.*;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UsersController {

    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UpdatePasswordValidator personValidator;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> showUserInfo() {
        User user = UserService.getCurrentUser();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }

    @PatchMapping("/password")
    public ResponseEntity<HttpStatus> updatePassword(@RequestBody UpdatePersonDTO updatePersonDTO,
                                                     BindingResult bindingResult) {
        User currentUser = UserService.getCurrentUser();
        personValidator.validate(updatePersonDTO, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.USER_EXCEPTION);
        currentUser.setPassword(passwordEncoder.encode(updatePersonDTO.getNewPassword()));
        userService.updateUser(currentUser.getId(), currentUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/username")
    public ResponseEntity<HttpStatus> updateUsername(@RequestBody Map<String, String> newUsername) {
        User user = UserService.getCurrentUser();

        String username = newUsername.get("username");

        if(username == null) {
            throw new UserException("Incorrect key");

        } else if (username.isBlank()) {
            throw new UserException("New username must not be empty");

        } else if (username.length() < 2 || username.length() > 20) {
            throw new UserException("New username must be between 2 and 20 characters");

        } else if (userService.getUserByUserName(username).isPresent() && !username.equals(user.getUsername())) {
            throw new UserException("Username taken");

        } else if (username.equals(user.getUsername())) {
            throw new UserException("New username must be different from the old one");
        }

        user.setUsername(username);
        userService.updateUser(user.getId(), user);

        return new ResponseEntity<>(HttpStatus.OK);
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

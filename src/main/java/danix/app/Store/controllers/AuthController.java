package danix.app.Store.controllers;

import danix.app.Store.dto.AcceptEmailKeyDTO;
import danix.app.Store.dto.AuthDTO;
import danix.app.Store.dto.UserDTO;
import danix.app.Store.dto.RecoverPasswordDTO;
import danix.app.Store.models.TokenStatus;
import danix.app.Store.models.User;
import danix.app.Store.repositories.EmailKeysRepository;
import danix.app.Store.security.JWTUtil;
import danix.app.Store.services.UserService;
import danix.app.Store.services.TokensService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

import static danix.app.Store.services.UserService.getCurrentUser;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final RegistrationValidator validator;
    private final AuthenticationProvider authenticationProvider;
    private final AuthValidator authValidator;
    private final TokensService tokensService;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailKeysRepository emailKeysRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid AuthDTO authDTO,
                                            BindingResult bindingResult) {
        authValidator.validate(authDTO, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.AUTHENTICATION_EXCEPTION);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword());
        try {
            authenticationProvider.authenticate(authToken);
        }catch (BadCredentialsException e) {
            return new ResponseEntity<>(Map.of("error", "Incorrect password") ,HttpStatus.BAD_REQUEST);
        }

        String token = jwtUtil.generateToken(authDTO.getEmail());
        tokensService.create(token, userService.getUserByEmail(authDTO.getEmail()).get());

        return new ResponseEntity<>(Map.of("jwt-token", token), HttpStatus.OK);
    }

    @GetMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> email) {
        if (email.get("email") == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        userService.getUserByEmail(email.get("email"))
                .orElseThrow(() -> new UserException("User not found"));
        emailKeysRepository.findByEmail(email.get("email")).ifPresent(key -> {
           if (key.getExpiredTime().isAfter(LocalDateTime.now())) {
               throw new AuthenticationException("User already have an active key");
           }
           userService.deleteEmailKey(key);
        });
        userService.sendRecoverPasswordKey(email.get("email"));
        return ResponseEntity.ok("Success");
    }

    @PatchMapping("/recoverPassword")
    public ResponseEntity<String> recoverPassword(@RequestBody RecoverPasswordDTO recoverPasswordDTO,
                                                  BindingResult bindingResult, @RequestParam(value = "key") int key) {
        ErrorHandler.handleException(bindingResult, ExceptionType.USER_EXCEPTION);
        emailKeysRepository.findByEmail(recoverPasswordDTO.getEmail()).ifPresentOrElse(emailKey -> {
            if (emailKey.getKey() != key) {
                userService.updateEmailKeyAttempts(emailKey);
                if (emailKey.getAttempts() >= 3) {
                    userService.deleteEmailKey(emailKey);
                    throw new AuthenticationException("The limit of attempts has been exceeded, send the key again");
                }
                throw new AuthenticationException("Invalid key");
            } else if (emailKey.getExpiredTime().isBefore(LocalDateTime.now())) {
                userService.deleteEmailKey(emailKey);
                throw new AuthenticationException("Expired key");
            }
            User user = userService.getUserByEmail(recoverPasswordDTO.getEmail()).get();
            user.setPassword(passwordEncoder.encode(recoverPasswordDTO.getNewPassword()));
            userService.updateUser(user.getId(), user);
            userService.deleteEmailKey(emailKey);
        }, () -> {
            throw new AuthenticationException("Email not found");
        });
        return ResponseEntity.ok("Password recovered");
    }

    @PostMapping("/registration")
    public ResponseEntity<String> registration(@RequestBody @Valid UserDTO userDTO,
                                                   BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.AUTHENTICATION_EXCEPTION);
        emailKeysRepository.findByEmail(userDTO.getEmail()).ifPresent(key -> {
            if (key.getExpiredTime().isAfter(LocalDateTime.now())) {
                throw new AuthenticationException("User already have an active key");
            }
            userService.deleteEmailKey(key);
            userService.deleteTemUser(userDTO.getEmail());
        });
        userService.deleteTemUser(userDTO.getEmail());
        validator.validate(userDTO, bindingResult);
        userService.temporalRegisterUser(userDTO);
        userService.sendRegistrationKey(userDTO.getEmail());
        return ResponseEntity.ok("Key sent");
    }

    @PatchMapping("/register-user")
    public ResponseEntity<String> registerUser(@RequestBody @Valid AcceptEmailKeyDTO emailKey,
                                               BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.AUTHENTICATION_EXCEPTION);
        emailKeysRepository.findByEmail(emailKey.getEmail()).ifPresentOrElse(key -> {
            if (emailKey.getKey() != key.getKey()) {
                userService.updateEmailKeyAttempts(key);
                if (key.getAttempts() >= 3) {
                    throw new AuthenticationException("The limit of attempts has been exceeded, send the key again");
                }
                throw new AuthenticationException("Invalid key");
            } else if (key.getExpiredTime().isBefore(LocalDateTime.now())) {
                userService.deleteEmailKey(key);
                userService.deleteTemUser(emailKey.getEmail());
                throw new AuthenticationException("Key is expired");
            }
            userService.register(emailKey.getEmail());
            userService.deleteEmailKey(key);
        }, () -> {
            throw new AuthenticationException("User not found");
        });
        return ResponseEntity.ok("Registration succeeded");
    }

    @PostMapping("/logout")
    public ResponseEntity<HttpStatus> logout() {
        User user = getCurrentUser();
        tokensService.getAllUserTokens(user).forEach(token -> tokensService.updateStatus(token.getId(), TokenStatus.REVOKED));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> exceptionHandle(UserException e) {
        ErrorResponse personErrorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(personErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> exceptionHandle(AuthenticationException e) {
        ErrorResponse personErrorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(personErrorResponse, HttpStatus.BAD_REQUEST);
    }

}

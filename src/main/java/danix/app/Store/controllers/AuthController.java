package danix.app.Store.controllers;

import danix.app.Store.dto.AcceptEmailKeyDTO;
import danix.app.Store.dto.AuthDTO;
import danix.app.Store.dto.RegistrationUserDTO;
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
    private final RegistrationValidator registrationValidator;
    private final AuthenticationProvider authenticationProvider;
    private final AuthValidator authValidator;
    private final TokensService tokensService;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailKeysRepository emailKeysRepository;
    private final EmailKeyValidator emailKeyValidator;

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

    @PostMapping("/password/key")
    public ResponseEntity<HttpStatus> forgotPassword(@RequestBody Map<String, String> emailData) {
        String email = emailData.get("email");
        if (email == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        userService.getUserByEmail(email)
                .orElseThrow(() -> new UserException("User not found"));
        emailKeysRepository.findByEmail(email).ifPresent(key -> {
           if (key.getExpiredTime().isAfter(LocalDateTime.now())) {
               throw new AuthenticationException("User already have an active key");
           }
           userService.deleteEmailKey(key);
        });
        userService.sendRecoverPasswordKey(email);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/password")
    public ResponseEntity<HttpStatus> recoverPassword(@RequestBody @Valid RecoverPasswordDTO recoverPasswordDTO,
                                                  BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.AUTHENTICATION_EXCEPTION);
        emailKeyValidator.validate(recoverPasswordDTO, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.AUTHENTICATION_EXCEPTION);
        User user = userService.getUserByEmail(recoverPasswordDTO.getEmail())
                        .orElseThrow(() -> new AuthenticationException("User not found"));
        user.setPassword(passwordEncoder.encode(recoverPasswordDTO.getNewPassword()));
        userService.updateUser(user.getId(), user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/registration")
    public ResponseEntity<HttpStatus> registration(@RequestBody @Valid RegistrationUserDTO registrationUserDTO,
                                                   BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.AUTHENTICATION_EXCEPTION);
        registrationValidator.validate(registrationUserDTO, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.AUTHENTICATION_EXCEPTION);
        userService.deleteTempUser(registrationUserDTO.getEmail());
        userService.temporalRegisterUser(registrationUserDTO);
        userService.sendRegistrationKey(registrationUserDTO.getEmail());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/registration/accept")
    public ResponseEntity<HttpStatus> registerUser(@RequestBody @Valid AcceptEmailKeyDTO emailKey,
                                               BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.AUTHENTICATION_EXCEPTION);
        emailKeyValidator.validate(emailKey, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.AUTHENTICATION_EXCEPTION);
        userService.register(emailKey.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
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

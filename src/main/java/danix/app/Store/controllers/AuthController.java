package danix.app.Store.controllers;

import danix.app.Store.dto.AuthDTO;
import danix.app.Store.dto.PersonDTO;
import danix.app.Store.dto.RecoverPasswordDTO;
import danix.app.Store.models.Person;
import danix.app.Store.models.TokenStatus;
import danix.app.Store.security.JWTUtil;
import danix.app.Store.services.PersonService;
import danix.app.Store.services.TokensService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final PersonService personService;
    private final PersonValidator validator;
    private final AuthenticationProvider authenticationProvider;
    private final AuthValidator authValidator;
    private final TokensService tokensService;
    private final JWTUtil jwtUtil;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(PersonService personService, PersonValidator validator, AuthenticationProvider authenticationProvider,
                          AuthValidator authValidator, TokensService tokensService, JWTUtil jwtUtil,
                          KafkaTemplate<String, String> kafkaTemplate, PasswordEncoder passwordEncoder) {
        this.personService = personService;
        this.validator = validator;
        this.authenticationProvider = authenticationProvider;
        this.authValidator = authValidator;
        this.tokensService = tokensService;
        this.jwtUtil = jwtUtil;
        this.kafkaTemplate = kafkaTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid AuthDTO authDTO,
                                            BindingResult bindingResult) {
        authValidator.validate(authDTO, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.USER_EXCEPTION);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword());
        try {
            authenticationProvider.authenticate(authToken);
        }catch (BadCredentialsException e) {
            return new ResponseEntity<>(Map.of("error", "Incorrect password") ,HttpStatus.BAD_REQUEST);
        }

        String token = jwtUtil.generateToken(authDTO.getEmail());
        tokensService.create(token, personService.getUserByEmail(authDTO.getEmail()).get());

        return new ResponseEntity<>(Map.of("jwt-token", token), HttpStatus.OK);
    }

    @GetMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> email) {
        if (email.get("email") == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        personService.getUserByEmail(email.get("email"))
                .orElseThrow(() -> new UserException("User not found"));
        if (PersonService.emailPasswordRecoverKeysMap.get(email.get("email")) != null
                && PersonService.emailPasswordRecoverKeysMap.get(email.get("email")).getExpiredAt().after(new Date())) {
            throw new UserException("You already have an active key");
        }
        kafkaTemplate.send("recoverPassword-topic", email.get("email"));
        return ResponseEntity.ok("Success");
    }

    @PatchMapping("/recoverPassword")
    public ResponseEntity<String> recoverPassword(@RequestBody RecoverPasswordDTO recoverPasswordDTO,
                                                  BindingResult bindingResult, @RequestParam(value = "key") int key) {
        ErrorHandler.handleException(bindingResult, ExceptionType.USER_EXCEPTION);
        Person user = personService.getUserByEmail(recoverPasswordDTO.getEmail())
                .orElseThrow(() -> new UserException("User not found"));
        if (PersonService.emailPasswordRecoverKeysMap.get(recoverPasswordDTO.getEmail()) == null) {
            throw new UserException("User not found");
        } else if (PersonService.emailPasswordRecoverKeysMap.get(recoverPasswordDTO.getEmail()).getKey() != key) {
            throw new UserException("Invalid key");
        } else if (PersonService.emailPasswordRecoverKeysMap.get(recoverPasswordDTO.getEmail()).getExpiredAt().before(new Date())) {
            PersonService.emailPasswordRecoverKeysMap.remove(recoverPasswordDTO.getEmail());
            throw new UserException("Key is expired");
        }
        user.setPassword(passwordEncoder.encode(recoverPasswordDTO.getNewPassword()));
        personService.updateUser(user.getId(), user);
        return ResponseEntity.ok("Password recovered");
    }

    @PostMapping("/registration")
    public ResponseEntity<String> registration(@RequestBody @Valid PersonDTO personDTO,
                                                   BindingResult bindingResult) {
        validator.validate(personDTO, bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.USER_EXCEPTION);
        if (PersonService.emailRegistrationKeysMap.get(personDTO.getEmail()) != null
        && PersonService.emailRegistrationKeysMap.get(personDTO.getEmail()).getExpiredAt().after(new Date())) {
            throw new UserException("You already have an active key");
        }
        personService.temporalRegisterUser(personDTO);
        kafkaTemplate.send("registrationCode-topic", personDTO.getEmail());
        return ResponseEntity.ok("Key sent");
    }

    @PostMapping("/register-user")
    public ResponseEntity<String> registerUser(@RequestParam(value = "email") String email,
                                               @RequestParam(value = "key") int key) {
        if (PersonService.emailRegistrationKeysMap.get(email) == null) {
            throw new UserException("User not found");
        } else if (PersonService.emailRegistrationKeysMap.get(email).getKey() != key) {
            throw new UserException("Invalid key");
        } else if (PersonService.emailRegistrationKeysMap.get(email).getExpiredAt().before(new Date())) {
            PersonService.emailRegistrationKeysMap.remove(email);
            throw new UserException("Time is up");
        }
        personService.register(email);
        return ResponseEntity.ok("Registration succeeded");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            tokensService.updateStatus(jwtUtil.getIdFromToken(jwt), TokenStatus.REVOKED);
            return ResponseEntity.ok("Logout successful");
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> exceptionHandle(UserException e) {
        ErrorResponse personErrorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(personErrorResponse, HttpStatus.BAD_REQUEST);
    }

}

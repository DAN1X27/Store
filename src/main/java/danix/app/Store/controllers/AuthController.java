package danix.app.Store.controllers;

import danix.app.Store.dto.AuthDTO;
import danix.app.Store.dto.PersonDTO;
import danix.app.Store.models.Person;
import danix.app.Store.models.TokenStatus;
import danix.app.Store.security.JWTUtil;
import danix.app.Store.services.PersonService;
import danix.app.Store.services.TokensService;
import danix.app.Store.services.EmailSenderServiceImpl;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final PersonService personService;
    private final PersonValidator validator;
    private final ModelMapper modelMapper;
    private final AuthenticationProvider authenticationProvider;
    private final AuthValidator authValidator;
    private final TokensService tokensService;
    private final EmailSenderServiceImpl emailSenderService;

    private final JWTUtil jwtUtil;

    @Autowired
    public AuthController(PersonService personService, PersonValidator validator,
                          ModelMapper modelMapper, AuthenticationProvider authenticationProvider,
                          AuthValidator authValidator, TokensService tokensService, EmailSenderServiceImpl emailSenderService, JWTUtil jwtUtil) {
        this.personService = personService;
        this.validator = validator;
        this.modelMapper = modelMapper;
        this.authenticationProvider = authenticationProvider;
        this.authValidator = authValidator;
        this.tokensService = tokensService;
        this.emailSenderService = emailSenderService;
        this.jwtUtil = jwtUtil;
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
        emailSenderService.sendEmail(
                email.get("email"),
                "Spring-store-application",
                // Add your link to front end
                "Your link to recover password: " + "http://localhost:8080/user/recoverPassword"
        );
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/registration")
    public ResponseEntity<Map<String, String>> registration(@RequestBody @Valid PersonDTO personDTO,
                                                   BindingResult bindingResult) {
        validator.validate(personDTO, bindingResult);

        ErrorHandler.handleException(bindingResult, ExceptionType.USER_EXCEPTION);

        Person person = convertToPerson(personDTO);

        personService.register(person);

        String token = jwtUtil.generateToken(person.getEmail());
        tokensService.create(token, person);
        emailSenderService.sendEmail(
                person.getEmail(),
                "Spring-store-application",
                "Successfully registered new account with email: " + person.getEmail()
        );
        return new ResponseEntity<>(Map.of("jwt-token", token), HttpStatus.OK);
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

    private Person convertToPerson(PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }
}

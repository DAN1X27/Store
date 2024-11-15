package danix.app.Store.controllers;

import danix.app.Store.dto.AuthDTO;
import danix.app.Store.dto.PersonDTO;
import danix.app.Store.models.Person;
import danix.app.Store.models.Token;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.rmi.server.UID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final PersonService personService;
    private final PersonValidator validator;
    private final ModelMapper modelMapper;
    private final AuthenticationProvider authenticationProvider;
    private final AuthValidator authValidator;
    private final TokensService tokensService;

    private final JWTUtil jwtUtil;

    @Autowired
    public AuthController(PersonService personService, PersonValidator validator,
                          ModelMapper modelMapper, AuthenticationProvider authenticationProvider,
                          AuthValidator authValidator, TokensService tokensService, JWTUtil jwtUtil) {
        this.personService = personService;
        this.validator = validator;
        this.modelMapper = modelMapper;
        this.authenticationProvider = authenticationProvider;
        this.authValidator = authValidator;
        this.tokensService = tokensService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid AuthDTO authDTO,
                                            BindingResult bindingResult) {
        authValidator.validate(authDTO, bindingResult);

        UserErrorHandler.exceptionHandle(bindingResult);

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

    @PostMapping("/registration")
    public ResponseEntity<Map<String, String>> registration(@RequestBody @Valid PersonDTO personDTO,
                                                   BindingResult bindingResult) {
        validator.validate(personDTO, bindingResult);

        UserErrorHandler.exceptionHandle(bindingResult);

        Person person = convertToPerson(personDTO);

        personService.register(person);

        String token = jwtUtil.generateToken(person.getEmail());
        tokensService.create(token, person);

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

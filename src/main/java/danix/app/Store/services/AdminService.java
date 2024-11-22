package danix.app.Store.services;

import danix.app.Store.dto.ResponsePersonDTO;
import danix.app.Store.models.Person;
import danix.app.Store.models.Token;
import danix.app.Store.models.TokenStatus;
import danix.app.Store.repositories.PersonRepository;
import danix.app.Store.security.JWTUtil;
import danix.app.Store.util.UserException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService {
    private final PersonRepository personRepository;
    private final OrderService orderService;
    private final TokensService tokensService;

    @Autowired
    public AdminService(PersonRepository personRepository, OrderService orderService,
                        TokensService tokensService) {
        this.personRepository = personRepository;
        this.orderService = orderService;
        this.tokensService = tokensService;
    }

    public List<ResponsePersonDTO> getAllUsers() {
        return personRepository.findAll().stream().map(this::convertToResponsePersonDTO).collect(Collectors.toList());
    }

    public Optional<ResponsePersonDTO> findPersonByUsername(String username) {
        return personRepository.findByUserName(username).map(this::convertToResponsePersonDTO);
    }

    @Transactional
    public void banUser(Person person) {

       if (person.isBanned()) {
           throw new UserException("User is already banned");
       }
       for (Token token : tokensService.getAllUserTokens(person)) {
           token.setStatus(TokenStatus.REVOKED);
       }
       person.setBanned(true);

    }

    @Transactional
    public void unbanUser(Person person) {

        if (!person.isBanned()) {
            throw new UserException("User is not banned");
        }
        for (Token token : tokensService.getAllUserTokens(person)) {
            token.setStatus(TokenStatus.ISSUED);
        }
        person.setBanned(false);

    }

    private ResponsePersonDTO convertToResponsePersonDTO(Person person) {
        ResponsePersonDTO responsePersonDTO = new ResponsePersonDTO();

        responsePersonDTO.setOrders(person.getOrders().stream()
                .map(orderService::convertToAdminOrderDTO).collect(Collectors.toList()));

        responsePersonDTO.setUsername(person.getUserName());
        responsePersonDTO.setEmail(person.getEmail());
        responsePersonDTO.setRole(person.getRole());
        responsePersonDTO.setCreatedAt(person.getCreatedAt());
        responsePersonDTO.setBanned(person.isBanned());

        return responsePersonDTO;
    }
}

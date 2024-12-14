package danix.app.Store.services;

import danix.app.Store.dto.ResponseUserDTO;
import danix.app.Store.models.User;
import danix.app.Store.models.Token;
import danix.app.Store.models.TokenStatus;
import danix.app.Store.repositories.UsersRepository;
import danix.app.Store.util.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {
    private final UsersRepository usersRepository;
    private final OrderService orderService;
    private final TokensService tokensService;

    public List<ResponseUserDTO> getAllUsers() {
        return usersRepository.findAll().stream().map(this::convertToResponsePersonDTO).collect(Collectors.toList());
    }

    public Optional<ResponseUserDTO> findPersonByUsername(String username) {
        return usersRepository.findByUsername(username).map(this::convertToResponsePersonDTO);
    }

    @Transactional
    public void banUser(User user) {

       if (user.getStatus() == User.Status.BANNED) {
           throw new UserException("User is already banned");
       }
       for (Token token : tokensService.getAllUserTokens(user)) {
           token.setStatus(TokenStatus.REVOKED);
       }
       user.setStatus(User.Status.BANNED);

    }

    @Transactional
    public void unbanUser(User user) {

        if (user.getStatus() != User.Status.BANNED) {
            throw new UserException("User is not banned");
        }
        for (Token token : tokensService.getAllUserTokens(user)) {
            token.setStatus(TokenStatus.ISSUED);
        }
        user.setStatus(User.Status.REGISTERED);

    }

    private ResponseUserDTO convertToResponsePersonDTO(User user) {
        ResponseUserDTO responseUserDTO = new ResponseUserDTO();

        responseUserDTO.setOrders(user.getOrders().stream()
                .map(orderService::convertToAdminOrderDTO).collect(Collectors.toList()));

        responseUserDTO.setUsername(user.getUsername());
        responseUserDTO.setEmail(user.getEmail());
        responseUserDTO.setRole(user.getRole());
        responseUserDTO.setCreatedAt(user.getCreatedAt());
        responseUserDTO.setStatus(user.getStatus());

        return responseUserDTO;
    }
}

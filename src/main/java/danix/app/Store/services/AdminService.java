package danix.app.Store.services;

import danix.app.Store.dto.ResponseUserDTO;
import danix.app.Store.models.User;
import danix.app.Store.models.Token;
import danix.app.Store.models.TokenStatus;
import danix.app.Store.repositories.UsersRepository;
import danix.app.Store.util.UserException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final ModelMapper modelMapper;

    public List<ResponseUserDTO> getAllUsers(int page, int count) {
        return usersRepository.findAll(PageRequest.of(page, count, Sort.by(Sort.Direction.DESC, "id"))).stream()
                .map(this::convertToResponseUserDTO)
                .collect(Collectors.toList());
    }

    public Optional<ResponseUserDTO> findUserByUsername(String username) {
        return usersRepository.findByUsername(username)
                .map(this::convertToResponseUserDTO);
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

    private ResponseUserDTO convertToResponseUserDTO(User user) {
        ResponseUserDTO responseUserDTO = modelMapper.map(user, ResponseUserDTO.class);
        responseUserDTO.setOrders(user.getOrders().stream()
                .map(orderService::convertToAdminOrderDTO).collect(Collectors.toList()));
        return responseUserDTO;
    }
}

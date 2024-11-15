package danix.app.Store.services;

import danix.app.Store.models.Person;
import danix.app.Store.models.Token;
import danix.app.Store.models.TokenStatus;
import danix.app.Store.repositories.TokensRepository;
import danix.app.Store.security.JWTUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.Tokens;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static danix.app.Store.models.TokenStatus.REVOKED;

@Service
@Transactional(readOnly = true)
public class TokensService {
    private final TokensRepository tokensRepository;
    private final JWTUtil jwtUtil;

    public TokensService(TokensRepository tokensRepository, JWTUtil jwtUtil) {
        this.tokensRepository = tokensRepository;
        this.jwtUtil = jwtUtil;
    }

    public boolean isValid(String id) {
        return tokensRepository.findById(id).orElseThrow(() -> new IllegalStateException("Invalid token"))
                .getStatus() != REVOKED;
    }

    public List<Token> getAllUserTokens(Person owner) {
        return tokensRepository.findAllByOwner(owner);
    }

    @Transactional
    public void updateStatus(String id, TokenStatus status) {
        Token token = tokensRepository.findById(id).orElseThrow(() -> new IllegalStateException("Invalid token"));
        token.setId(id);
        token.setStatus(status);
    }

    @Transactional
    public void create(String token, Person owner) {
        Token tokenToSave = new Token();
        tokenToSave.setId(jwtUtil.getIdFromToken(token));
        tokenToSave.setStatus(TokenStatus.ISSUED);
        tokenToSave.setOwner(owner);
        tokensRepository.save(tokenToSave);
    }

}

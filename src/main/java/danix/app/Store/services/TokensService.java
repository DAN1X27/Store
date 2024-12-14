package danix.app.Store.services;

import danix.app.Store.models.User;
import danix.app.Store.models.Token;
import danix.app.Store.models.TokenStatus;
import danix.app.Store.repositories.TokensRepository;
import danix.app.Store.security.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static danix.app.Store.models.TokenStatus.REVOKED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TokensService {
    private final TokensRepository tokensRepository;
    private final JWTUtil jwtUtil;

    public boolean isValid(String id) {
        return tokensRepository.findById(id).orElseThrow(() -> new IllegalStateException("Invalid token"))
                .getStatus() != REVOKED;
    }

    public List<Token> getAllUserTokens(User owner) {
        return tokensRepository.findAllByOwner(owner);
    }

    @Transactional
    public void updateStatus(String id, TokenStatus status) {
        Token token = tokensRepository.findById(id).orElseThrow(() -> new IllegalStateException("Invalid token"));
        token.setId(id);
        token.setStatus(status);
    }

    @Transactional
    public void create(String token, User owner) {
        Token tokenToSave = new Token();
        tokenToSave.setId(jwtUtil.getIdFromToken(token));
        tokenToSave.setStatus(TokenStatus.ISSUED);
        tokenToSave.setOwner(owner);
        tokenToSave.setExpiredDate(Date.from(ZonedDateTime.now().plusDays(14).toInstant()));
        tokensRepository.save(tokenToSave);
    }

}

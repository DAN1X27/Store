package danix.app.Store.task;

import danix.app.Store.repositories.TokensRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteExpiredJWTTokensTask {
    private final TokensRepository tokensRepository;

    @Transactional
    @Scheduled(cron = "@hourly")
    public void run() {
        tokensRepository.deleteExpiredTokens();
    }
}

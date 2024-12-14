package danix.app.Store.task;

import danix.app.Store.models.User;
import danix.app.Store.repositories.EmailKeysRepository;
import danix.app.Store.repositories.UsersRepository;
import danix.app.Store.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class CleanTemporalResourcesTask {
    private final UsersRepository usersRepository;
    private final EmailKeysRepository emailKeysRepository;

    @Transactional
    @Scheduled(cron = "@hourly")
    public void deleteTemporalUsers() {
        usersRepository.deleteAllByStatusAndCreatedAtBefore(User.Status.TEMPORAL_REGISTERED, LocalDateTime.now().minusDays(1));
    }

    @Transactional
    @Scheduled(cron = "@hourly")
    public void deleteExpiredEmailKeys() {
        emailKeysRepository.deleteExpiredEmailKeys();
    }
}

package danix.app.Store.task;

import danix.app.Store.services.PersonService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CleanTemporalResourcesTask {

    @Scheduled(cron = "@hourly")
    public void deleteExpiredPasswordRecoverKeys() {
        for (String email : PersonService.emailPasswordRecoverKeysMap.keySet()) {
            if (PersonService.emailPasswordRecoverKeysMap.get(email).getExpiredAt().before(new Date())) {
                PersonService.emailPasswordRecoverKeysMap.remove(email);
            }
        }
        System.out.println("Expired keys deleted!");
    }

    @Scheduled(cron = "@hourly")
    public void deleteExpiredTemporalUsers() {
        for (String email : PersonService.temporalUsersMap.keySet()) {
            if (PersonService.temporalUsersMap.get(email).getExpiredTime().before(new Date())) {
                PersonService.temporalUsersMap.remove(email);
            }
        }
        System.out.println("Expired temporal users deleted!");
    }

    @Scheduled(cron = "@hourly")
    public void deleteExpiredRegistrationKeys() {
        for (String email : PersonService.emailRegistrationKeysMap.keySet()) {
            if (PersonService.emailRegistrationKeysMap.get(email).getExpiredAt().before(new Date())) {
                PersonService.emailRegistrationKeysMap.remove(email);
            }
        }
        System.out.println("Expired registration keys deleted!");
    }
}

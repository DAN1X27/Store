package danix.app.Store.util;

import danix.app.Store.dto.RequestEmailKey;
import danix.app.Store.repositories.EmailKeysRepository;
import danix.app.Store.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EmailKeyValidator implements Validator {
    private final UserService userService;
    private final EmailKeysRepository emailKeysRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RequestEmailKey.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RequestEmailKey requestEmailKey = (RequestEmailKey) target;

        emailKeysRepository.findByEmail(requestEmailKey.getEmail()).ifPresentOrElse(key -> {
            if (!requestEmailKey.getKey().equals(key.getKey())) {
                userService.updateEmailKeyAttempts(key);
                if (key.getAttempts() >= 3) {
                    userService.deleteEmailKey(key);
                    userService.deleteTempUser(requestEmailKey.getEmail());
                    errors.rejectValue("key", "", "The limit of attempts has been exceeded, send the key again");
                } else {
                    errors.rejectValue("key", "", "Invalid key");
                }
            } else if (key.getExpiredTime().isBefore(LocalDateTime.now())) {
                userService.deleteEmailKey(key);
                userService.deleteTempUser(requestEmailKey.getEmail());
                errors.rejectValue("key", "", "Expired Key");
            } else {
                userService.deleteEmailKey(key);
            }
        }, () -> errors.rejectValue("email", "", "Email not found"));
    }
}

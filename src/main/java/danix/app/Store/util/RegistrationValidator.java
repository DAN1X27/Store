package danix.app.Store.util;

import danix.app.Store.dto.RegistrationUserDTO;
import danix.app.Store.repositories.EmailKeysRepository;
import danix.app.Store.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RegistrationValidator implements Validator {
    private final UserService userService;
    private final EmailKeysRepository emailKeysRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RegistrationUserDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegistrationUserDTO registrationUserDTO = (RegistrationUserDTO) target;

        emailKeysRepository.findByEmail(registrationUserDTO.getEmail()).ifPresentOrElse(key -> {
            if (key.getExpiredTime().isAfter(LocalDateTime.now())) {
                errors.rejectValue("email", "", "User already have an active key");
            } else {
                userService.deleteEmailKey(key);
                userService.deleteTempUser(registrationUserDTO.getEmail());
            }
        }, () -> {
            userService.getUserByEmail(registrationUserDTO.getEmail())
                    .ifPresent(user -> errors.rejectValue("email", "", "Email is busy"));
            userService.getUserByUserName(registrationUserDTO.getUsername())
                    .ifPresent(user -> errors.rejectValue("username", "", "Username is already in use"));
        });
    }
}

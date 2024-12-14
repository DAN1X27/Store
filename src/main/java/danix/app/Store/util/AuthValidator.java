package danix.app.Store.util;

import danix.app.Store.dto.AuthDTO;
import danix.app.Store.models.User;
import danix.app.Store.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class AuthValidator implements Validator {
    private final UserService userService;

    @Autowired
    public AuthValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return AuthDTO.class.equals(clazz);
    }

    @Override
    public void  validate(Object target, Errors errors) {
        AuthDTO authDTO = (AuthDTO) target;
        userService.getUserByEmail(authDTO.getEmail()).ifPresentOrElse(user -> {
            if (user.getStatus() == User.Status.BANNED) {
                errors.rejectValue("email", "", "Account is banned");
            } else if (user.getStatus() == User.Status.TEMPORAL_REGISTERED) {
                errors.rejectValue("email", "", "User not found");
            }
        }, () -> {
            errors.rejectValue("email", "", "User not found");
        });
    }
}

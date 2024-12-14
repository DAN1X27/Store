package danix.app.Store.util;

import danix.app.Store.dto.UpdatePersonDTO;
import danix.app.Store.models.User;
import danix.app.Store.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UpdatePasswordValidator implements Validator {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UpdatePasswordValidator(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdatePersonDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User currentUser = UserService.getCurrentUser();
        UpdatePersonDTO updatePersonDTO = (UpdatePersonDTO) target;

        if (!passwordEncoder.matches(updatePersonDTO.getOldPassword(), currentUser.getPassword())) {
            errors.rejectValue("oldPassword", "", "Incorrect password!");
        } else if (passwordEncoder.matches(updatePersonDTO.getNewPassword(), currentUser.getPassword())) {
            errors.rejectValue("newPassword", "", "The new password must be different from the old one");
        }
    }
}

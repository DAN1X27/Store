package danix.app.Store.util;

import danix.app.Store.dto.UserDTO;
import danix.app.Store.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RegistrationValidator implements Validator {
    private final UserService userService;

    @Autowired
    public RegistrationValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDTO userDTO = (UserDTO) target;

        if(userService.getUserByUserName(userDTO.getUsername()).isPresent()) {
            errors.rejectValue("userName", "",
                    "Username already in use");
            return;
        }

        if(userService.getUserByEmail(userDTO.getEmail()).isPresent()) {
            errors.rejectValue("email", "",
                    "Email already in use");

        }
    }
}

package danix.app.Store.util;

import danix.app.Store.dto.UpdatePersonDTO;
import danix.app.Store.models.Person;
import danix.app.Store.services.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UpdatePasswordValidator implements Validator {
    private final PersonService personService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UpdatePasswordValidator(PersonService personService, PasswordEncoder passwordEncoder) {
        this.personService = personService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdatePersonDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Person currentUser = PersonService.getCurrentUser();
        UpdatePersonDTO updatePersonDTO = (UpdatePersonDTO) target;

        if (!passwordEncoder.matches(updatePersonDTO.getOldPassword(), currentUser.getPassword())) {
            System.out.println(currentUser.getPassword());
            System.out.println(passwordEncoder.encode(updatePersonDTO.getOldPassword()));
            errors.rejectValue("oldPassword", "", "Incorrect password!");
        } else if (passwordEncoder.matches(updatePersonDTO.getNewPassword(), currentUser.getPassword())) {
            errors.rejectValue("newPassword", "", "The new password must be different from the old one");
        }
    }
}

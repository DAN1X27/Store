package danix.app.Store.util;

import danix.app.Store.dto.AuthDTO;
import danix.app.Store.dto.PersonDTO;
import danix.app.Store.models.Person;
import danix.app.Store.services.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class AuthValidator implements Validator {
    private final PersonService personService;

    @Autowired
    public AuthValidator(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return AuthDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AuthDTO authDTO = (AuthDTO) target;

        Optional<Person> person = personService.getUserByEmail(authDTO.getEmail());

        if(person.isEmpty()) {
            errors.rejectValue("email", "", "User not found");
        }

        if(person.isPresent() && person.get().isBanned()) {
            errors.rejectValue("email", "", "Account banned");
        }
    }
}

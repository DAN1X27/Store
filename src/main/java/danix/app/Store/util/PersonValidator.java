package danix.app.Store.util;

import danix.app.Store.dto.PersonDTO;
import danix.app.Store.services.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PersonValidator implements Validator {
    private final PersonService personService;

    @Autowired
    public PersonValidator(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return PersonDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PersonDTO personDTO = (PersonDTO) target;

        if(personService.getUserByUserName(personDTO.getUsername()).isPresent()) {
            errors.rejectValue("userName", "",
                    "Person with the same username already exists.");
            return;
        }

        if(personService.getUserByEmail(personDTO.getEmail()).isPresent()) {
            errors.rejectValue("email", "",
                    "Person with the same email already exist.");

        }
    }
}

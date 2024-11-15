package danix.app.Store.services;

import danix.app.Store.models.Person;
import danix.app.Store.repositories.PersonRepository;
import danix.app.Store.security.PersonDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PersonService {
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PersonService(PersonRepository personRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(Person person) {
        enrichUser(person);
        personRepository.save(person);
    }

    public Optional<Person> getUserByUserName(String userName) {
        return personRepository.findByUserName(userName);
    }

    public Optional<Person> getUserByEmail(String email) {
        return personRepository.findByEmail(email);
    }

    public static Person getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        return personDetails.getPerson();
    }

    @Transactional
    public void updateUser(Integer id, Person person) {
        person.setId(id);
        personRepository.save(person);
    }

    private void enrichUser(Person person) {
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        person.setCreatedAt(LocalDateTime.now());
        person.setRole("ROLE_USER");
        person.setBanned(false);
    }
}

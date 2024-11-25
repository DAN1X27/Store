package danix.app.Store.services;

import danix.app.Store.dto.PersonDTO;
import danix.app.Store.models.EmailKeys;
import danix.app.Store.models.Person;
import danix.app.Store.models.TemporalUser;
import danix.app.Store.repositories.PersonRepository;
import danix.app.Store.security.PersonDetails;
import danix.app.Store.util.UserException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(readOnly = true)
public class PersonService {
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public static final Map<String, EmailKeys> emailPasswordRecoverKeysMap = new ConcurrentHashMap<>();
    public static final Map<String, EmailKeys> emailRegistrationKeysMap = new ConcurrentHashMap<>();
    public static final Map<String, TemporalUser> temporalUsersMap = new ConcurrentHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            emailPasswordRecoverKeysMap.clear();
            emailRegistrationKeysMap.clear();
            temporalUsersMap.clear();
        }));
    }

    @Autowired
    public PersonService(PersonRepository personRepository, @Lazy PasswordEncoder passwordEncoder,
                         ModelMapper modelMapper) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public void register(String email) {
        if (temporalUsersMap.get(email) == null) {
            throw new UserException("User not found");
        }else if (temporalUsersMap.get(email).getExpiredTime().before(new Date())) {
            temporalUsersMap.remove(email);
            throw new UserException("Time is up");
        }
        Person person = modelMapper.map(temporalUsersMap.get(email).getUser(), Person.class);
        enrichUser(person);
        personRepository.save(person);
        temporalUsersMap.remove(person.getEmail());
        emailRegistrationKeysMap.remove(email);
    }

    public void temporalRegisterUser(PersonDTO personDTO) {
        temporalUsersMap.put(personDTO.getEmail(), new TemporalUser(personDTO, Date.from(ZonedDateTime.now().plusMinutes(2).toInstant())));
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

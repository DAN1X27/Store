package danix.app.Store.services;

import danix.app.Store.dto.RegistrationUserDTO;
import danix.app.Store.models.EmailKey;
import danix.app.Store.models.User;
import danix.app.Store.repositories.EmailKeysRepository;
import danix.app.Store.repositories.UsersRepository;
import danix.app.Store.security.PersonDetails;
import danix.app.Store.util.UserException;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EmailKeysRepository emailKeysRepository;

    public UserService(UsersRepository usersRepository, @Lazy PasswordEncoder passwordEncoder,
                       KafkaTemplate<String, String> kafkaTemplate, EmailKeysRepository emailKeysRepository) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
        this.emailKeysRepository = emailKeysRepository;
    }

    @Transactional
    public void register(String email) {
        User user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found"));
        if (user.getStatus() == User.Status.TEMPORAL_REGISTERED) {
            user.setStatus(User.Status.REGISTERED);
        }
    }

    @Transactional
    public void temporalRegisterUser(RegistrationUserDTO registrationUserDTO) {
        usersRepository.save(
                User.builder()
                        .username(registrationUserDTO.getUsername())
                        .email(registrationUserDTO.getEmail())
                        .createdAt(LocalDateTime.now())
                        .status(User.Status.TEMPORAL_REGISTERED)
                        .password(passwordEncoder.encode(registrationUserDTO.getPassword()))
                        .role(User.Roles.ROLE_USER)
                        .build()
        );
    }

    @Transactional
    public void updateEmailKeyAttempts(EmailKey key) {
        key.setAttempts(key.getAttempts() + 1);
    }

    @Transactional
    public void sendRegistrationKey(String email) {
        EmailKey emailKey = generateEmailKey(email);
        emailKeysRepository.save(emailKey);
        kafkaTemplate.send("registration-topic", email, String.valueOf(emailKey.getKey()));
    }

    @Transactional
    public void sendRecoverPasswordKey(String email) {
        EmailKey emailKey = generateEmailKey(email);
        emailKeysRepository.save(emailKey);
        kafkaTemplate.send("recover-password-topic", email, String.valueOf(emailKey.getKey()));
    }

    private EmailKey generateEmailKey(String email) {
        Random random = new Random();
        int key = random.nextInt(100000, 999999);
        return EmailKey.builder()
                .key(key)
                .expiredTime(LocalDateTime.now().plusMinutes(3))
                .email(email)
                .attempts(0)
                .build();
    }

    @Transactional
    public void deleteTempUser(String email) {
        usersRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() == User.Status.TEMPORAL_REGISTERED) {
                usersRepository.delete(user);
            }
        });
    }

    @Transactional
    public void deleteEmailKey(EmailKey emailKey) {
        emailKeysRepository.delete(emailKey);
    }

    public Optional<User> getUserByUserName(String userName) {
        return usersRepository.findByUsername(userName);
    }

    public Optional<User> getUserByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        return personDetails.getPerson();
    }

    @Transactional
    public void updateUser(Integer id, User user) {
        user.setId(id);
        usersRepository.save(user);
    }
}

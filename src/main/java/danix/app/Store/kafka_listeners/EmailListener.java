package danix.app.Store.kafka_listeners;

import danix.app.Store.models.EmailKeys;
import danix.app.Store.services.EmailSenderService;
import danix.app.Store.services.PersonService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Random;

@Component
@EnableKafka
public class EmailListener {

    private final EmailSenderService emailSenderService;

    @Autowired
    public EmailListener(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @KafkaListener(topics = "recoverPassword-topic")
    public void recoverPasswordListener(String email) {
        Random random = new Random();
        int key = random.nextInt(100000, 999999);
        PersonService.emailPasswordRecoverKeysMap.put(email, new EmailKeys(key,
                Date.from(ZonedDateTime.now().plusMinutes(2).toInstant())));
        emailSenderService.sendMessage(
                email,
                "Your code to recover password: " + key
        );
    }

    @KafkaListener(topics = "registrationCode-topic")
    public void registrationCodeListener(String email) {
        Random random = new Random();
        int key = random.nextInt(100000, 999999);
        PersonService.emailRegistrationKeysMap.put(email,
                new EmailKeys(key, Date.from(ZonedDateTime.now().plusMinutes(2).toInstant())));
        emailSenderService.sendMessage(
                email,
                "Your code to register for registration: " + key
        );
    }

    @KafkaListener(topics = "banUser-topic")
    public void banUserListener(ConsumerRecord<String, String> record) {
        emailSenderService.sendMessage(
                record.key(),
                "Your account has been banned for reason: " + record.value()
        );
    }

    @KafkaListener(topics = "unbanUser-topic")
    public void unbanUserListener(String email) {
        emailSenderService.sendMessage(
                email,
                "Your account has been unbanned!"
        );
    }
}

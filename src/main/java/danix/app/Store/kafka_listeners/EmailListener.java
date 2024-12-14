package danix.app.Store.kafka_listeners;

import danix.app.Store.models.EmailKey;
import danix.app.Store.services.EmailSenderService;
import danix.app.Store.services.UserService;
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

    @KafkaListener(topics = "recover-password-topic")
    public void recoverPasswordListener(ConsumerRecord<String, String> record) {
        emailSenderService.sendMessage(
                record.key(),
                "Your code to recover password: " + record.value()
        );
    }

    @KafkaListener(topics = "registration-topic")
    public void registrationCodeListener(ConsumerRecord<String, String> record) {
        emailSenderService.sendMessage(
                record.key(),
                "Your code to register for registration: " + record.value()
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

package danix.app.Store.kafka_listeners;

import danix.app.Store.services.EmailSenderService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
                "Your registration code: " + record.value()
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

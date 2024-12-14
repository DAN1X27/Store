package danix.app.Store.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;

    public void sendMessage(String to, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("danikcheban2007@gmail.com"); //enter your email
        message.setTo(to);
        message.setText(text);
        message.setSubject("Spring-store-application");
        mailSender.send(message);
    }
}

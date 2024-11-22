package danix.app.Store.services;

public interface EmailSenderService {
    void sendEmail(String to, String subject, String message);
}

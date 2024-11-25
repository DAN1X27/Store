package danix.app.Store.task;

import danix.app.Store.models.Order;
import danix.app.Store.repositories.OrderRepository;
import danix.app.Store.services.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotifyingUsersAboutOrdersStatus {
    private final OrderRepository orderRepository;
    private final EmailSenderService emailSenderService;

    @Autowired
    public NotifyingUsersAboutOrdersStatus(OrderRepository orderRepository, EmailSenderService emailSenderService) {
        this.orderRepository = orderRepository;
        this.emailSenderService = emailSenderService;
    }

    @Scheduled(cron = "@midnight")
    public void run() {
        List<Order> orders = orderRepository.findAllOrders();
        for (Order order : orders) {
            if (order.isReady()) {
                emailSenderService.sendMessage(
                        order.getOwner().getEmail(),
                        "Your order with id: " + order.getId() + " is ready, you can take him. " +
                                "Storage date: " + order.getStorageDate()
                );
            }
        }
    }
}

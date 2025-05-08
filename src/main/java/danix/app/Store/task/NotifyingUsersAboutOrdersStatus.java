package danix.app.Store.task;

import danix.app.Store.models.Order;
import danix.app.Store.repositories.IdProjection;
import danix.app.Store.repositories.OrdersRepository;
import danix.app.Store.services.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotifyingUsersAboutOrdersStatus {
    private final OrdersRepository ordersRepository;
    private final EmailSenderService emailSenderService;

    @Scheduled(cron = "@midnight")
    public void run() {
        int page = 0;
        while (true) {
            List<Order> orders = ordersRepository.findAllByReadyTrue(PageRequest.of(page, 50));
            if (orders.isEmpty()) {
                break;
            }
            for (Order order : orders) {
                emailSenderService.sendMessage(
                        order.getOwner().getEmail(),
                        "Your order with id: " + order.getId() + " is ready, you can take him. " +
                        "Storage date: " + order.getStorageDate());
            }
            page++;
        }
    }
}

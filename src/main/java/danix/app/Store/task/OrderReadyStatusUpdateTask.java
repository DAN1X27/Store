package danix.app.Store.task;

import danix.app.Store.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderReadyStatusUpdateTask {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderReadyStatusUpdateTask(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Scheduled(cron = "@hourly")
    public void run() {
        orderRepository.updateReadyStatus();
    }
}

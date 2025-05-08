package danix.app.Store.task;

import danix.app.Store.repositories.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderReadyStatusUpdateTask {

    private final OrdersRepository ordersRepository;

    @Autowired
    public OrderReadyStatusUpdateTask(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @Transactional
    @Scheduled(cron = "@hourly")
    public void run() {
        ordersRepository.updateReadyStatus();
    }
}

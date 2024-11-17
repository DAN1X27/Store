package danix.app.Store.task;

import danix.app.Store.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
public class OutdatedOderCleanerTask {
    private final OrderRepository orderRepository;

    @Autowired
    public OutdatedOderCleanerTask(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Scheduled(cron = "@midnight")
    public void run() {
        orderRepository.deleteAllByStorageDateLessThan(new Date());
    }
}

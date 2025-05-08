package danix.app.Store.task;

import danix.app.Store.repositories.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
public class OutdatedOderCleanerTask {
    private final OrdersRepository ordersRepository;

    @Autowired
    public OutdatedOderCleanerTask(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @Transactional
    @Scheduled(cron = "@midnight")
    public void run() {
        ordersRepository.deleteAllByStorageDateLessThan(new Date());
    }
}

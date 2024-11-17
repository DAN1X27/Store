package danix.app.Store.repositories;

import danix.app.Store.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    void deleteAllByStorageDateLessThan(Date date);

    @Modifying
    @Query("update Order set isReady=true where orderReadyDate <= current_date")
    void updateReadyStatus();
}

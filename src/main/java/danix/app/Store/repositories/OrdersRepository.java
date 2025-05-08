package danix.app.Store.repositories;

import danix.app.Store.models.Order;
import danix.app.Store.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Order, Integer> {
    void deleteAllByStorageDateLessThan(Date date);

    @Modifying
    @Query("update Order set isReady=true where orderReadyDate <= current_date")
    void updateReadyStatus();

    @Query("select o from Order o left join o.items where o.id in (:ids)")
    List<Order> findAllByIdIn(List<Integer> ids, Sort sort);

    @Query("select o.id from Order o")
    List<IdProjection> findAllOrders(Pageable pageable);

    List<Order> findAllByReadyTrue(Pageable pageable);

    List<IdProjection> findAllByOwner(User owner, Pageable pageable);
}

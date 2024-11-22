package danix.app.Store.repositories;

import danix.app.Store.models.Order;
import danix.app.Store.models.Person;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    void deleteAllByStorageDateLessThan(Date date);

    @Modifying
    @Query("update Order set isReady=true where orderReadyDate <= current_date")
    void updateReadyStatus();

    @Query("select o from Order o left join fetch o.items")
    List<Order> findAllOrders();

    @Query("select o from Order o left join fetch o.items i where o.owner = :owner")
    List<Order> finByOwner(@Param("owner") Person owner);
}

package danix.app.Store.repositories;

import danix.app.Store.models.Order;
import danix.app.Store.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findAllByOwner(Person owner);
}

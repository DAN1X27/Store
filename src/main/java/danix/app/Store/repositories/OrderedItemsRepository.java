package danix.app.Store.repositories;

import danix.app.Store.models.Item;
import danix.app.Store.models.Order;
import danix.app.Store.models.OrderedItems;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderedItemsRepository extends JpaRepository<OrderedItems, Integer> {
    List<OrderedItems> findAllByOrder(Order order);
}

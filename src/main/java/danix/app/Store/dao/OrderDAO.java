package danix.app.Store.dao;

import danix.app.Store.models.Order;
import danix.app.Store.models.OrderedItems;
import danix.app.Store.models.Person;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class OrderDAO {
    private final EntityManager entityManager;

    @Autowired
    public OrderDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Order> getAllOrders() {
        Session session = entityManager.unwrap(Session.class);

        List<Order> orders = session.createQuery("select o from Order o LEFT JOIN fetch o.items").getResultList();

        return orders;
    }

    public List<Order> findAllByOwner(Person owner) {
        Session session = entityManager.unwrap(Session.class);

        List<Order> orders = session.createQuery("select o from Order o left join fetch o.items where o.owner.id=:id")
                .setParameter("id", owner.getId()).getResultList();

        return orders;
    }
}

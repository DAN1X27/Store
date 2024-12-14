package danix.app.Store.services;

import danix.app.Store.models.Order;
import danix.app.Store.models.OrderedItems;
import danix.app.Store.repositories.OrderedItemsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderedItemsService {
    private final OrderedItemsRepository orderedItemsRepository;

    @Transactional
    public void save(OrderedItems orderedItems) {
        orderedItemsRepository.save(orderedItems);
    }

    public List<OrderedItems> getByOrder(Order order) {
        return orderedItemsRepository.findAllByOrder(order);
    }

}

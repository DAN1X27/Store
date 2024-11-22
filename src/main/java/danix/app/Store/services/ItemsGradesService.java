package danix.app.Store.services;

import danix.app.Store.models.Item;
import danix.app.Store.models.ItemGrade;
import danix.app.Store.models.Person;
import danix.app.Store.repositories.ItemsGradesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ItemsGradesService {
    private final ItemsGradesRepository itemsGradesRepository;

    @Autowired
    public ItemsGradesService(ItemsGradesRepository itemsGradesRepository) {
        this.itemsGradesRepository = itemsGradesRepository;
    }

    public List<ItemGrade> getAllByItem(Item item) {
        return itemsGradesRepository.findAllByItem(item);
    }

    public Optional<ItemGrade> getByItemAndOwner(Item item, Person owner) {
        return itemsGradesRepository.findByItemAndOwner(item, owner);
    }

    @Transactional
    public void addItemGrade(ItemGrade itemGrade) {
        itemsGradesRepository.save(itemGrade);
    }
}

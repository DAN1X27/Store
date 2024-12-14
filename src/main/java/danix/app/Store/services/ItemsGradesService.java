package danix.app.Store.services;

import danix.app.Store.models.Item;
import danix.app.Store.models.ItemGrade;
import danix.app.Store.models.User;
import danix.app.Store.repositories.ItemsGradesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemsGradesService {
    private final ItemsGradesRepository itemsGradesRepository;

    public List<ItemGrade> getAllByItem(Item item) {
        return itemsGradesRepository.findAllByItem(item);
    }

    public Optional<ItemGrade> getByItemAndOwner(Item item, User owner) {
        return itemsGradesRepository.findByItemAndOwner(item, owner);
    }

    @Transactional
    public void addItemGrade(ItemGrade itemGrade) {
        itemsGradesRepository.save(itemGrade);
    }
}

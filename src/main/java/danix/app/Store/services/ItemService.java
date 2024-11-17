package danix.app.Store.services;

import danix.app.Store.models.Item;
import danix.app.Store.repositories.ItemRepository;
import danix.app.Store.util.ItemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> getALlItems() {
        return itemRepository.findAll();
    }

    public Item getItemByName(String name) {
        return itemRepository.findByName(name).orElseThrow(() -> new ItemException("Item not found"));
    }

    public List<Item> getAllItemsSortedByPrice() {
        return getALlItems().stream().sorted(new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getPrice().compareTo(o2.getPrice());
            }
        }).collect(Collectors.toList());
    }

    @Transactional
    public void addItem(Item item) {
        Optional<Item> item1 = itemRepository.findByName(item.getName());

        if(item1.isEmpty()){
            itemRepository.save(item);
        }else {
           item1.get().setCount(item1.get().getCount() + item.getCount());
        }
    }

    @Transactional
    public void deleteItem(Item item) {
        if (item.getCount() == 0) {
            itemRepository.delete(item);
        }else {
            Optional<Item> item1 = itemRepository.findByName(item.getName());
            Integer count = item1.get().getCount();
            item1.get().setCount(--count);
        }
    }

    @Transactional
    public void updateItem(Integer id ,Item item) {
        item.setId(id);
        itemRepository.save(item);
    }
}

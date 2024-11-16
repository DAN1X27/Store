package danix.app.Store.util;

import danix.app.Store.dto.CartDTO;
import danix.app.Store.dto.ItemDTO;
import danix.app.Store.models.Item;
import danix.app.Store.repositories.ItemRepository;
import danix.app.Store.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class CartValidator implements Validator {
    private final ItemRepository itemRepository;
    @Autowired
    public CartValidator(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return CartDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CartDTO cartDTO = (CartDTO) target;

        for (ItemDTO item : cartDTO.getItems()) {
            Optional<Item> item1 = itemRepository.findByName(item.getName());
            if(item1.isEmpty()) {
                errors.rejectValue("items", "", "Item " + item.getName() + " not found");
                break;

            } else if (item1.get().getCount() < item.getCount()) {
                errors.rejectValue("items", "", "The count of items cannot be more than what is in stock");
            }
        }
    }
}

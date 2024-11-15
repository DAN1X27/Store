package danix.app.Store.util;

import danix.app.Store.models.Item;
import danix.app.Store.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {
    private final ItemService itemService;

    @Autowired
    public ItemValidator(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        String itemName = (String) target;

        if(itemService.findItemByName(itemName).isEmpty()) {
            errors.rejectValue("name", "", "Item not found");
        }
    }
}

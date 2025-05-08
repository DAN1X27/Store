package danix.app.Store.util;

import danix.app.Store.dto.OrderDTO;
import danix.app.Store.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class OrderValidator implements Validator {
    private final ItemService itemService;

    @Autowired
    public OrderValidator(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return OrderDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OrderDTO order = (OrderDTO) target;

        order.getItems().forEach(item -> {
            if(item.getName() == null) {
                errors.rejectValue("items", "", "Item name must not be empty");
                return;
            }

            if(item.getCount() == null) {
                errors.rejectValue("items", "", "Items count must not be empty");
                return;
            }

            if((item.getName()).isEmpty()) {
                errors.rejectValue("items", "", "Item " + item.getName() + " not found.");
            } else if (item.getCount() > itemService.getItemByName(item.getName()).getCount()) {
                errors.rejectValue("items", "", "The count number of items is more than what is in stock");
            }
        });
    }
}

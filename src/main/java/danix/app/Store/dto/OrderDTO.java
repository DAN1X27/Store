package danix.app.Store.dto;

import danix.app.Store.models.Item;
import danix.app.Store.models.Person;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderDTO {
    @NotEmpty(message = "List of items must not be empty")
    private List<ItemDTO> items;

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }
}

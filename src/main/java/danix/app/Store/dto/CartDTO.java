package danix.app.Store.dto;

import danix.app.Store.models.Item;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CartDTO {

    @NotNull(message = "List of items must not be empty")
    private List<ItemDTO> items;

    public @NotNull(message = "List of items must not be empty") List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(@NotNull(message = "List of items must not be empty") List<ItemDTO> items) {
        this.items = items;
    }
}

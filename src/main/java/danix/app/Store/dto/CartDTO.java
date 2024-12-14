package danix.app.Store.dto;

import danix.app.Store.models.Item;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartDTO {

    @NotNull(message = "List of items must not be empty")
    private List<ItemDTO> items;
}

package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderDTO {
    @NotEmpty(message = "List of items must not be empty")
    private List<ItemDTO> items;
}

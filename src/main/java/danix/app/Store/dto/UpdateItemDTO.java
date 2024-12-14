package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateItemDTO {
    @NotEmpty(message = "Name of the item must not be empty")
    private String name;
    @NotNull(message = "New item to save must not be null")
    private SaveItemDTO saveItem;
}

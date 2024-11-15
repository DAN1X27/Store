package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class UpdateItemDTO {
    @NotEmpty(message = "Name of the item must not be empty")
    private String name;
    @NotNull(message = "New item to save must not be null")
    private SaveItemDTO saveItem;

    public SaveItemDTO getSaveItem() {
        return saveItem;
    }

    public void setSaveItem(SaveItemDTO saveItem) {
        this.saveItem = saveItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

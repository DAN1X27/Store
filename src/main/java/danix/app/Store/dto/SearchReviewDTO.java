package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;

public class SearchReviewDTO {

    @NotEmpty(message = "Item name must not be empty")
    private String itemName;

    @NotEmpty(message = "Username must not be empty")
    private String username;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

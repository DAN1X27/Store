package danix.app.Store.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class AddGradeDTO {

    @NotNull(message = "Items grade must not be empty")
    @Min(value = 1, message = "Grade must be between 1 and 5")
    @Max(value = 5, message = "Grade must be between 1 and 5")
    private Integer grade;

    @NotEmpty(message = "Item name must not be empty")
    private String itemName;

    public @NotNull(message = "Items grade must not be empty") @Min(value = 1, message = "Grade must be between 1 and 5") @Max(value = 5, message = "Grade must be between 1 and 5") Integer getGrade() {
        return grade;
    }

    public void setGrade(@NotNull(message = "Items grade must not be empty") @Min(value = 1, message = "Grade must be between 1 and 5") @Max(value = 5, message = "Grade must be between 1 and 5") Integer grade) {
        this.grade = grade;
    }

    public @NotEmpty(message = "Item name must not be empty") String getItemName() {
        return itemName;
    }

    public void setItemName(@NotEmpty(message = "Item name must not be empty") String itemName) {
        this.itemName = itemName;
    }
}

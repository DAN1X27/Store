package danix.app.Store.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddGradeDTO {

    @NotNull(message = "Items grade must not be empty")
    @Min(value = 1, message = "Grade must be between 1 and 5")
    @Max(value = 5, message = "Grade must be between 1 and 5")
    private Integer grade;

    @NotEmpty(message = "Item name must not be empty")
    private String itemName;
}

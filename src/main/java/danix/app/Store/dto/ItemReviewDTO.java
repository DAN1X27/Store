package danix.app.Store.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemReviewDTO {

    @NotEmpty(message = "Comment must not be empty")
    private String comment;

    @NotEmpty(message = "Item name must not be empty")
    private String itemName;

    @NotNull(message = "Items grade must not be empty")
    @Min(value = 1, message = "Grade cannot be less then 1")
    @Max(value = 5, message = "Grade cannot be more then 5")
    private Integer grade;
}

package danix.app.Store.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Data
public class SaveItemDTO {
    @NotEmpty(message = "Item name must not be empty")
    private String name;

    @NotNull(message = "Price must not be empty")
    @Min(value = 0, message = "Price cannot be less than zero")
    private Double price;

    @Column(name = "count")
    @Min(value = 0, message = "Count cannot be lass than zero")
    @NotNull(message = "Items count must not be empty")
    private Integer count;

    @NotEmpty(message = "Category must not be empty")
    private String category;

    @NotEmpty(message = "Description must not be empty")
    @Size(min = 5, max = 150, message = "Description must be between 5 and 150 characters")
    private String description;
}

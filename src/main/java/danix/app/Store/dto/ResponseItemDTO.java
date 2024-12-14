package danix.app.Store.dto;

import danix.app.Store.models.CategoryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseItemDTO {

    private String name;
    private Double price;
    private Integer count;
    private Integer reviewsCount;
    private CategoryType categoryType;
    private Double rating;
    private int id;
}

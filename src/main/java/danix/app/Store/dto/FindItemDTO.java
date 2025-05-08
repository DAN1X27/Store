package danix.app.Store.dto;

import danix.app.Store.models.CategoryType;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindItemDTO {
    private Integer id;
    private String name;
    private Integer count;
    private Double price;
    private List<ResponseItemReviewDTO> reviews;
    private CategoryType category;
    private String description;
    private Double rating;
    private Integer userGrade;
    private List<ItemImageIdDTO> images;
}

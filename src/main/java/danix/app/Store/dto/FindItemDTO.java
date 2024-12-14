package danix.app.Store.dto;

import danix.app.Store.models.CategoryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FindItemDTO {
    private Integer id;
    private String name;
    private Integer count;
    private Double price;
    private List<ResponseItemReviewsDTO> reviews;
    private CategoryType category;
    private String description;
    private Double rating;
    private Integer userGrade;
    private List<ItemImageIdDTO> images;

    public static Builder builder() {
        return new Builder();
    }

    public FindItemDTO(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.count = builder.count;
        this.price = builder.price;
        this.reviews = builder.reviews;
        this.category = builder.category;
        this.description = builder.description;
        this.rating = builder.rating;
        this.userGrade = builder.userGrade;
        this.images = builder.images;
    }

    public static class Builder {
        private Integer id;
        private String name;
        private Integer count;
        private Double price;
        private List<ResponseItemReviewsDTO> reviews;
        private CategoryType category;
        private String description;
        private Double rating;
        private Integer userGrade;
        private List<ItemImageIdDTO> images;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder count(Integer count) {
            this.count = count;
            return this;
        }

        public Builder price(Double price) {
            this.price = price;
            return this;
        }

        public Builder reviews(List<ResponseItemReviewsDTO> reviews) {
            this.reviews = reviews;
            return this;
        }

        public Builder category(CategoryType category) {
            this.category = category;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder rating(Double rating) {
            this.rating = rating;
            return this;
        }

        public Builder userGrade(Integer userGrade) {
            this.userGrade = userGrade;
            return this;
        }

        public Builder images(List<ItemImageIdDTO> images) {
            this.images = images;
            return this;
        }

        public FindItemDTO build() {
            return new FindItemDTO(this);
        }
    }
}

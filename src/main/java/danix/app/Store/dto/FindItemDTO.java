package danix.app.Store.dto;

import java.util.List;

public class FindItemDTO {

    private String name;

    private Integer count;

    private Double price;

    private List<ResponseItemReviewsDTO> reviews;

    private String category;

    private String description;

    private Double rating;

    private Integer userGrade;

    public Integer getUserGrade() {
        return userGrade;
    }

    public void setUserGrade(Integer userGrade) {
        this.userGrade = userGrade;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<ResponseItemReviewsDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<ResponseItemReviewsDTO> reviews) {
        this.reviews = reviews;
    }
}

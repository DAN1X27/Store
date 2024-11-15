package danix.app.Store.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class SaveItemDTO {
    @NotEmpty(message = "Item name must not be empty")
    private String name;

    @NotNull(message = "Price must not be empty")
    @Min(value = 0, message = "Price cannot be less than zero")
    private Double price;

    @Column(name = "count")
    @Min(value = 0, message = "Count cannot be lass than zero")
    @NotNull(message = "Items count must not be null")
    private Integer count;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", count=" + count +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaveItemDTO that = (SaveItemDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(price, that.price) && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, count);
    }
}

package danix.app.Store.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;

public class DeleteItemDTO {
    @NotEmpty(message = "Item name must not be empty")
    private String name;

    @Column(name = "count")
    private Integer count;

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
}

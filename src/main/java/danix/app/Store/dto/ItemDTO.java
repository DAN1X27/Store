package danix.app.Store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ItemDTO {

    private String name;
    private Integer count;

    public ItemDTO(String name, Integer count) {
        this.name = name;
        this.count = count;
    }

    public ItemDTO() {}

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

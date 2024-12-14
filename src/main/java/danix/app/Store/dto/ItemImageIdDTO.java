package danix.app.Store.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemImageIdDTO {
    Long id;

    public ItemImageIdDTO(Long id) {
        this.id = id;
    }
}

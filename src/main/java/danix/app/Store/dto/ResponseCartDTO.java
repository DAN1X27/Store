package danix.app.Store.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseCartDTO {
    private List<SaveItemDTO> items;
    private double price;
}

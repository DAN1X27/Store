package danix.app.Store.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ResponseOrderDTO {
    private Integer id;
    private List<SaveItemDTO> items;
    private Double sum;
    private Date orderReadyDate;
    private Date storageDate;
    private String isReady;
}

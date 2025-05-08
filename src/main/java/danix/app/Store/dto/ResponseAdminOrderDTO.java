package danix.app.Store.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ResponseAdminOrderDTO {

    private Integer id;
    private List<SaveItemDTO> items;
    private LocalDateTime createdAt;
    private String ownerUsername;
    private Double price;
    private Date orderReadyDate;
    private boolean isReady;
    private Date storageDate;
}

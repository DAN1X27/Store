package danix.app.Store.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
public class AdminOrderDTO {

    private Integer id;
    private List<SaveItemDTO> items;

    private LocalDateTime createdAt;

    private String ownerName;

    private Double sum;

    private Date orderReadyDate;
    private String isReady;

    private Date storageDate;
}

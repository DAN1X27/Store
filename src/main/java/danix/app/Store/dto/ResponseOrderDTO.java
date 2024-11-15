package danix.app.Store.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class ResponseOrderDTO {
    private Integer id;
    private List<SaveItemDTO> items;

    private Double sum;

    private Date orderReadyDate;

    private Date storageDate;
    private String isReady;

    public Date getStorageDate() {
        return storageDate;
    }

    public void setStorageDate(Date storageDate) {
        this.storageDate = storageDate;
    }

    public String getIsReady() {
        return isReady;
    }

    public void setIsReady(String isReady) {
        this.isReady = isReady;
    }

    public Double getSum() {
        return sum;
    }

    public Date getOrderReadyDate() {
        return orderReadyDate;
    }

    public void setOrderReadyDate(Date orderReadyDate) {
        this.orderReadyDate = orderReadyDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public List<SaveItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SaveItemDTO> items) {
        this.items = items;
    }
}

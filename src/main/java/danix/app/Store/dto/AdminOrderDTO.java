package danix.app.Store.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderDTO {

    private Integer id;
    private List<SaveItemDTO> items;

    private LocalDateTime createdAt;

    private String ownerName;

    private Double sum;

    private Date orderReadyDate;
    private String isReady;

    private Date storageDate;

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public List<SaveItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SaveItemDTO> items) {
        this.items = items;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

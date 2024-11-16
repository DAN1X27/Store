package danix.app.Store.dto;

import java.util.List;

public class ResponseCartDTO {

    private List<SaveItemDTO> items;

    private double price;

    public List<SaveItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SaveItemDTO> items) {
        this.items = items;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

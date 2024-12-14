package danix.app.Store.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Ordered_Items")
@Getter
@Setter
@NoArgsConstructor
public class OrderedItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    @Column(name = "items_count")
    private Integer count;

    public OrderedItems(Order order, Item item, Integer count) {
        this.order = order;
        this.item = item;
        this.count = count;
    }

    public String getItemName() {
        return this.item.getName();
    }
}

package danix.app.Store.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Ordered_Items")
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

    public String getName() {
        return item.getName();
    }

    public double getPrice() {
       return item.getPrice() * item.getCount();
    }

    public OrderedItems() {}

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}

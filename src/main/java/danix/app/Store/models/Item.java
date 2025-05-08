package danix.app.Store.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "Item")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Double price;

    @Column(name = "count")
    private Integer count;

    @ManyToMany(mappedBy = "items")
    private List<Order> orders;

    @ManyToMany(mappedBy = "items")
    private List<Cart> carts;

    @OneToMany(mappedBy = "item")
    private List<ItemReview> reviews;

    @Enumerated(EnumType.STRING)
    private CategoryType category;

    @Column(name = "description")
    private String description;

    @Column(name = "rating")
    private Double rating;

    @OneToMany(mappedBy = "item")
    private List<ItemGrade> itemGrades;

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE)
    private List<ItemImage> images;

}

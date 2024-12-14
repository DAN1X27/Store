package danix.app.Store.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "Items_Reviews")
@Getter
@Setter
public class ItemReviews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "comment")
    private String comment;

    @Column(name = "likes")
    private Integer likes;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "grade")
    private Integer grade;
}

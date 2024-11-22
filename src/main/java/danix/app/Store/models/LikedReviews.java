package danix.app.Store.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Liked_Reviews")
public class LikedReviews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private Person owner;

    @ManyToOne
    @JoinColumn(name = "review_id", referencedColumnName = "id")
    private ItemReviews itemReview;

    public LikedReviews(Person owner, ItemReviews itemReview) {
        this.owner = owner;
        this.itemReview = itemReview;
    }

    public LikedReviews() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public ItemReviews getItemReview() {
        return itemReview;
    }

    public void setItemReview(ItemReviews itemReview) {
        this.itemReview = itemReview;
    }

    public String getOwnerName() {
        return owner.getUserName();
    }
}

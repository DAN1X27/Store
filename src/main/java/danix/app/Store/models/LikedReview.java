package danix.app.Store.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Liked_Reviews")
@Getter
@Setter
@NoArgsConstructor
public class LikedReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "review_id", referencedColumnName = "id")
    private ItemReview itemReview;

    public LikedReview(User owner, ItemReview itemReview) {
        this.owner = owner;
        this.itemReview = itemReview;
    }
}

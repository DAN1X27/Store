package danix.app.Store.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "items_images")
@Getter
@Setter
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "image_uuid")
    private String imageUUID;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
}

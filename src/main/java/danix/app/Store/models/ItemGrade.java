package danix.app.Store.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Items_Grades")
@Getter
@Setter
@NoArgsConstructor
public class ItemGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    @ManyToOne()
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "grade")
    private int grade;

    public ItemGrade(Item item, int grade, User owner) {
        this.item = item;
        this.grade = grade;
        this.owner = owner;
    }
}

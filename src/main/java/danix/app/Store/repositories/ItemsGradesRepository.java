package danix.app.Store.repositories;

import danix.app.Store.models.Item;
import danix.app.Store.models.ItemGrade;
import danix.app.Store.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemsGradesRepository extends JpaRepository<ItemGrade, Integer> {
    List<ItemGrade> findAllByItem(Item item);

    Optional<ItemGrade> findByItemAndOwner(Item item, Person owner);
}

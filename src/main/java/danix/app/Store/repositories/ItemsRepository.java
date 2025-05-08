package danix.app.Store.repositories;

import danix.app.Store.models.CategoryType;
import danix.app.Store.models.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemsRepository extends JpaRepository<Item, Integer> {
    Optional<Item> findByName(String name);

    List<Item> findAllByCategory(CategoryType categoryType, Pageable pageable);
}

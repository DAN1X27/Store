package danix.app.Store.repositories;

import danix.app.Store.models.Item;
import danix.app.Store.models.ItemReview;
import danix.app.Store.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemReviewsRepository extends JpaRepository<ItemReview, Integer> {
    List<ItemReview> findByItem(Item item, Pageable pageable);

    List<ItemReview> findAllByOwner(User owner, Pageable pageable);

    Optional<ItemReview> findByOwnerAndItem(User owner, Item item);
}

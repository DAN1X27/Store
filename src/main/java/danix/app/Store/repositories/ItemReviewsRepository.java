package danix.app.Store.repositories;

import danix.app.Store.models.Item;
import danix.app.Store.models.ItemReviews;
import danix.app.Store.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemReviewsRepository extends JpaRepository<ItemReviews, Integer> {
    List<ItemReviews> findByItem(Item item);

    List<ItemReviews> findAllByOwner(User owner);

    Optional<ItemReviews> findByOwnerAndItem(User owner, Item item);
}

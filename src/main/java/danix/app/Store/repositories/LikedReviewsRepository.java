package danix.app.Store.repositories;

import danix.app.Store.models.ItemReviews;
import danix.app.Store.models.LikedReviews;
import danix.app.Store.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikedReviewsRepository extends JpaRepository<LikedReviews, Integer> {
    List<LikedReviews> findByItemReview(ItemReviews itemReview);

    List<LikedReviews> findByOwner(User owner);

    Optional<LikedReviews> findByItemReviewAndOwner(ItemReviews itemReview, User owner);
}

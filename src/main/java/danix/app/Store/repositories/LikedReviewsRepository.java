package danix.app.Store.repositories;

import danix.app.Store.models.ItemReview;
import danix.app.Store.models.LikedReview;
import danix.app.Store.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikedReviewsRepository extends JpaRepository<LikedReview, Integer> {

    Optional<LikedReview> findByItemReviewAndOwner(ItemReview itemReview, User owner);
}

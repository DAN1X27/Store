package danix.app.Store.services;

import danix.app.Store.models.ItemReviews;
import danix.app.Store.models.LikedReviews;
import danix.app.Store.models.User;
import danix.app.Store.repositories.LikedReviewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikedReviewsService {
    private final LikedReviewsRepository likedReviewsRepository;

    public List<LikedReviews> getAllByItemReview(ItemReviews itemReview) {
        return likedReviewsRepository.findByItemReview(itemReview);
    }

    public List<LikedReviews> getAllByOwner(User owner) {
        return likedReviewsRepository.findByOwner(owner);
    }

    @Transactional
    public void delete(LikedReviews likedReviews) {
        likedReviewsRepository.delete(likedReviews);
    }

    @Transactional
    public void save(LikedReviews likedReviews) {
        likedReviewsRepository.save(likedReviews);
    }

    public Optional<LikedReviews> findByItemReviewAndOwner(ItemReviews itemReview, User owner) {
        return likedReviewsRepository.findByItemReviewAndOwner(itemReview, owner);
    }
}

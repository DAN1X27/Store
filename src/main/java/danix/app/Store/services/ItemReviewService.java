package danix.app.Store.services;

import danix.app.Store.dto.ItemReviewDTO;
import danix.app.Store.dto.ResponseItemReviewDTO;
import danix.app.Store.models.*;
import danix.app.Store.repositories.ItemsRepository;
import danix.app.Store.repositories.ItemReviewsRepository;
import danix.app.Store.repositories.ItemsGradesRepository;
import danix.app.Store.repositories.LikedReviewsRepository;
import danix.app.Store.util.ReviewException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static danix.app.Store.services.UserService.getCurrentUser;
import static danix.app.Store.util.PageUtils.getPage;

@Service
@RequiredArgsConstructor
public class ItemReviewService {
    private final ItemReviewsRepository itemReviewsRepository;
    private final UserService userService;
    private final ItemsGradesRepository itemsGradesRepository;
    private final ItemsRepository itemsRepository;
    private final LikedReviewsRepository likedReviewsRepository;
    private final ModelMapper modelMapper;

    public List<ResponseItemReviewDTO> getItemReviews(Item item, int page, int count) {
        return itemReviewsRepository.findByItem(item, getPage(page, count, "likes")).stream()
                .map(this::convertToItemReviewDTO)
                .toList();
    }

    public ResponseItemReviewDTO getUserReviewForItem(Item item) {
        User currentUser = getCurrentUser();
        return itemReviewsRepository.findByOwnerAndItem(currentUser, item)
                .map(this::convertToItemReviewDTO)
                .orElse(null);
    }

    @Transactional
    public void deleteReview(Item item) {
        deleteReview(item, getCurrentUser());
    }

    @Transactional
    public void deleteReview(Item item, String userName) {
        User owner = userService.getUserByUserName(userName)
                .orElseThrow(() -> new ReviewException("User not found"));
        deleteReview(item, owner);
    }

    private void deleteReview(Item item, User owner) {
        ItemReview itemReview = itemReviewsRepository.findByOwnerAndItem(owner, item)
                .orElseThrow(() -> new ReviewException("Item review not found"));
        itemReviewsRepository.delete(itemReview);
    }

    @Transactional
    public void addLikeToReview(Item item, String ownerUsername) {
        User currentUser = getCurrentUser();
        User owner = userService.getUserByUserName(ownerUsername)
                .orElseThrow(() -> new ReviewException("Owner not found"));
        ItemReview itemReview = itemReviewsRepository.findByOwnerAndItem(owner, item)
                .orElseThrow(() -> new ReviewException("Item review not found"));
        likedReviewsRepository.findByItemReviewAndOwner(itemReview, owner)
                .ifPresentOrElse(likedReview -> {
                    itemReview.setLikes(itemReview.getLikes() - 1);
                    likedReviewsRepository.delete(likedReview);
                }, () -> {
                    itemReview.setLikes(itemReview.getLikes() + 1);
                    likedReviewsRepository.save(new LikedReview(currentUser, itemReview));
                });
    }

    @Transactional
    public void createReview(ItemReviewDTO itemReviewDTO, String itemName) {
        User currentUser = getCurrentUser();
        Item item = itemsRepository.findByName(itemName)
                .orElseThrow(() -> new ReviewException("Item not found"));
        itemReviewsRepository.findByOwnerAndItem(currentUser, item).ifPresent(review -> {
            throw new ReviewException("You already reviewed this item!");
        });
        if (itemReviewDTO.getGrade() < 1) throw new ReviewException("The grade cannot be less than 1");
        ItemReview review = ItemReview.builder()
                .item(item)
                .owner(currentUser)
                .createdAt(new Date())
                .likes(0)
                .comment(itemReviewDTO.getComment())
                .grade(itemReviewDTO.getGrade())
                .build();
        itemReviewsRepository.save(review);
        itemsGradesRepository.save(new ItemGrade(item, itemReviewDTO.getGrade(), currentUser));
        List<ItemGrade> itemGrades = itemsGradesRepository.findAllByItem(item);
        int sum = itemGrades.stream().mapToInt(ItemGrade::getGrade).sum();
        double rating = Math.round((double) sum / itemGrades.size() * 10.0) / 10.0;
        item.setRating(rating);
    }

    public List<ResponseItemReviewDTO> getAllUserReviews(int page, int count) {
        User currentUser = getCurrentUser();
        return itemReviewsRepository.findAllByOwner(currentUser, getPage(page, count, "id")).stream()
                .map(this::convertToItemReviewDTO)
                .toList();
    }

    public List<ResponseItemReviewDTO> getAllUserReviewsByAdmin(User user, int page, int count) {
        return itemReviewsRepository.findAllByOwner(user, getPage(page, count, "id")).stream()
                .map(this::convertToItemReviewDTO)
                .toList();
    }

    public ResponseItemReviewDTO convertToItemReviewDTO(ItemReview itemReview) {
        ResponseItemReviewDTO reviewDTO = modelMapper.map(itemReview, ResponseItemReviewDTO.class);
        likedReviewsRepository.findByItemReviewAndOwner(itemReview, getCurrentUser())
                .ifPresent(likedReview -> reviewDTO.setLiked(true));
        return reviewDTO;
    }
}

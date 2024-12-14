package danix.app.Store.services;

import danix.app.Store.dto.ItemReviewsDTO;
import danix.app.Store.dto.ResponseItemReviewsDTO;
import danix.app.Store.models.*;
import danix.app.Store.repositories.ItemReviewsRepository;
import danix.app.Store.repositories.ItemsGradesRepository;
import danix.app.Store.util.ReviewException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ItemReviewsService {
    private final ItemReviewsRepository itemReviewsRepository;
    private final LikedReviewsService likedReviewsService;
    private final UserService userService;
    private final ItemsGradesRepository itemsGradesRepository;
    private final ItemsGradesService itemsGradesService;

    @Autowired
    public ItemReviewsService(ItemReviewsRepository itemReviewsRepository, LikedReviewsService likedReviewsService,
                              UserService userService, ItemsGradesRepository itemsGradesRepository,
                              ItemsGradesService itemsGradesService) {
        this.itemReviewsRepository = itemReviewsRepository;
        this.likedReviewsService = likedReviewsService;
        this.userService = userService;
        this.itemsGradesRepository = itemsGradesRepository;
        this.itemsGradesService = itemsGradesService;
    }

    public List<ResponseItemReviewsDTO> getItemReviews(Item item) {
        return itemReviewsRepository.findByItem(item).stream()
                .map(this::convertToItemReviewsDTO)
                .toList().stream()
                .sorted((o1, o2) -> o2.getLikes().compareTo(o1.getLikes()))
                .collect(Collectors.toList());
    }

    public ResponseItemReviewsDTO getUserReviewForItem(Item item) {
        User currentUser = UserService.getCurrentUser();
        return itemReviewsRepository.findByOwnerAndItem(currentUser, item)
                .map(this::convertToItemReviewsDTO)
                .orElse(null);
    }

    @Transactional
    public void deleteReview(Item item) {
        deleteUserReview(item, UserService.getCurrentUser());
    }

    @Transactional
    public void deleteUserReview(Item item, String userName) {
        User owner = userService.getUserByUserName(userName)
                .orElseThrow(() -> new ReviewException("User not found"));
        deleteUserReview(item, owner);
    }

    private void deleteUserReview(Item item, User owner) {
        ItemReviews itemReview = itemReviewsRepository.findByOwnerAndItem(owner, item)
                .orElseThrow(() -> new ReviewException("Item review not found"));
        itemReviewsRepository.delete(itemReview);
    }

    @Transactional
    public void addLikeToReview(Item item, String ownerUsername) {
        User currentUser = UserService.getCurrentUser();
        User owner = userService.getUserByUserName(ownerUsername)
                .orElseThrow(() -> new ReviewException("Owner not found"));
        ItemReviews itemReview = itemReviewsRepository.findByOwnerAndItem(owner, item)
                .orElseThrow(() -> new ReviewException("Item review not found"));
        likedReviewsService.findByItemReviewAndOwner(itemReview, owner)
                .ifPresentOrElse(likedReview -> {
                    itemReview.setLikes(itemReview.getLikes() - 1);
                    likedReviewsService.delete(likedReview);
                }, () -> {
                    itemReview.setLikes(itemReview.getLikes() + 1);
                    likedReviewsService.save(new LikedReviews(currentUser, itemReview));
                });
    }

    @Transactional
    public void createReview(ItemReviewsDTO itemReviewsDTO, Item item) {
        itemReviewsRepository.findByOwnerAndItem(UserService.getCurrentUser(), item)
                .ifPresent(review -> {
                    throw new ReviewException("You are already reviewed this item!");
                });
        if (itemReviewsDTO.getGrade() < 1) throw new ReviewException("The grade cannot be less than 1");
        itemReviewsRepository.save(convertToItemReviews(itemReviewsDTO, item));
        itemsGradesRepository.save(new ItemGrade(item, itemReviewsDTO.getGrade(), UserService.getCurrentUser()));
        List<ItemGrade> itemGrades = itemsGradesService.getAllByItem(item);
        int sum = itemGrades.stream().mapToInt(ItemGrade::getGrade).sum();
        double rating = Math.round( (double) sum / itemGrades.size() * 10.0) / 10.0;
        item.setRating(rating);
    }

    public List<ResponseItemReviewsDTO> getAllUserReviews() {
        User currentUser = UserService.getCurrentUser();
        return itemReviewsRepository.findAllByOwner(currentUser).stream()
                .map(this::convertToItemReviewsDTO).toList();
    }

    public List<ResponseItemReviewsDTO> getAllUserReviewsForAdmin(User user) {
        return itemReviewsRepository.findAllByOwner(user).stream()
                .map(this::convertToItemReviewsDTO).toList();
    }

    public ResponseItemReviewsDTO convertToItemReviewsDTO(ItemReviews itemReviews) {
        ResponseItemReviewsDTO reviewDTO = new ResponseItemReviewsDTO();
        reviewDTO.setLikes(itemReviews.getLikes());
        reviewDTO.setComment(itemReviews.getComment());
        reviewDTO.setItemName(itemReviews.getItem().getName());
        reviewDTO.setCreatedAt(itemReviews.getCreatedAt());
        reviewDTO.setOwnerUsername(itemReviews.getOwner().getUsername());
        List<LikedReviews> likedReviews = likedReviewsService.getAllByItemReview(itemReviews);
        for (LikedReviews likedReview : likedReviews) {
            if (likedReview.getOwnerName().equals(UserService.getCurrentUser().getUsername())) {
                reviewDTO.setLiked(true);
            }
        }
        reviewDTO.setGrade(itemReviews.getGrade());
        return reviewDTO;
    }

    private ItemReviews convertToItemReviews(ItemReviewsDTO itemReviewsDTO, Item item) {
        ItemReviews itemReviews = new ItemReviews();
        itemReviews.setItem(item);
        itemReviews.setOwner(UserService.getCurrentUser());
        itemReviews.setComment(itemReviewsDTO.getComment());
        itemReviews.setCreatedAt(new Date());
        itemReviews.setLikes(0);
        itemReviews.setGrade(itemReviewsDTO.getGrade());
        return itemReviews;
    }
}

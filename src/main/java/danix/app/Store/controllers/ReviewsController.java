package danix.app.Store.controllers;

import danix.app.Store.dto.SearchReviewDTO;
import danix.app.Store.dto.ItemReviewDTO;
import danix.app.Store.dto.ResponseItemReviewDTO;
import danix.app.Store.models.Item;
import danix.app.Store.models.User;
import danix.app.Store.services.ItemReviewService;
import danix.app.Store.services.ItemService;
import danix.app.Store.services.UserService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewsController {
    private final ItemReviewService itemReviewService;
    private final ItemService itemService;
    private final UserService userService;

    @GetMapping
    public List<ResponseItemReviewDTO> getItemReviews(@RequestParam(value = "by_user", required = false) boolean byUser,
                                                      @RequestParam int page, @RequestParam int count,
                                                      @RequestBody Map<String, String> item) {
        Item searchItem = requestHelper(item);
        if (byUser) {
            return Collections.singletonList(itemReviewService.getUserReviewForItem(searchItem));
        }
        return itemReviewService.getItemReviews(searchItem, page, count);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> createReview(@RequestBody @Valid ItemReviewDTO itemReviewDTO, BindingResult bindingResult) {
        try {
            ErrorHandler.handleException(bindingResult, ExceptionType.REVIEW_EXCEPTION);
            itemReviewService.createReview(itemReviewDTO, itemReviewDTO.getItemName());
        } catch (ItemException e) {
            throw new ReviewException(e.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteReview(@RequestBody Map<String, String> item) {
        Item searchItem = requestHelper(item);
        itemReviewService.deleteReview(searchItem);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin")
    public ResponseEntity<HttpStatus> deleteReviewByAdmin(@RequestBody @Valid SearchReviewDTO reviewDTO,
                                                          BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.REVIEW_EXCEPTION);
        try {
            itemReviewService.deleteReview(itemService.getItemByName(reviewDTO.getItemName()), reviewDTO.getUsername());
        } catch (ItemException e) {
            throw new ReviewException(e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/like")
    public ResponseEntity<HttpStatus> addLike(@RequestBody @Valid SearchReviewDTO reviewDTO,
                                              BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.REVIEW_EXCEPTION);
        try {
            itemReviewService.addLikeToReview(itemService.getItemByName(reviewDTO.getItemName()), reviewDTO.getUsername());
        } catch (ItemException e) {
            throw new ReviewException(e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public List<ResponseItemReviewDTO> getAllUserReviewsForAdmin(@RequestBody Map<String, String> user,
                                                                 @RequestParam int page, @RequestParam int count) {
        User owner = userService.getUserByUserName(user.get("username"))
                .orElseThrow(() -> new ReviewException("User not found"));
        return itemReviewService.getAllUserReviewsByAdmin(owner, page, count);
    }

    @GetMapping("/user")
    public List<ResponseItemReviewDTO> getUserReviews(@RequestParam int page, @RequestParam int count) {
        return itemReviewService.getAllUserReviews(page, count);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(ReviewException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private Item requestHelper(Map<String, String> item) {
        if (item.get("itemName") == null) {
            throw new ReviewException("Incorrect key");
        }
        Item item1;
        try {
            item1 = itemService.getItemByName(item.get("itemName"));
        } catch (ItemException e) {
            throw new ReviewException(e.getMessage());
        }
        return item1;
    }
}

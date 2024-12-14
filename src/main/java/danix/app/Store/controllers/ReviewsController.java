package danix.app.Store.controllers;

import danix.app.Store.dto.SearchReviewDTO;
import danix.app.Store.dto.ItemReviewsDTO;
import danix.app.Store.dto.ResponseItemReviewsDTO;
import danix.app.Store.models.Item;
import danix.app.Store.models.User;
import danix.app.Store.services.ItemReviewsService;
import danix.app.Store.services.ItemService;
import danix.app.Store.services.UserService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ReviewsController {
    private final ItemReviewsService itemReviewsService;
    private final ItemService itemService;
    private final UserService userService;

    @Autowired
    public ReviewsController(ItemReviewsService itemReviewsService, ItemService itemService,
                             UserService userService) {
        this.itemReviewsService = itemReviewsService;
        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping
    public List<ResponseItemReviewsDTO> getItemReviews(@RequestParam(value = "getByUser", required = false) boolean getByUser,
                                                           @RequestBody Map<String, String> item) {
        Item searchItem = responseHelper(item);
        if (getByUser) {
           return Collections.singletonList(itemReviewsService.getUserReviewForItem(searchItem));
        }
        return itemReviewsService.getItemReviews(searchItem);
    }

    @PostMapping("/create")
    public ResponseEntity<HttpStatus> createReview(@RequestBody @Valid ItemReviewsDTO itemReviewsDTO, BindingResult bindingResult) {
        try {
            Item searchItem = itemService.getItemByName(itemReviewsDTO.getItemName());
            if (bindingResult.hasErrors()) {
                ErrorHandler.handleException(bindingResult, ExceptionType.REVIEW_EXCEPTION);
            }
            itemReviewsService.createReview(itemReviewsDTO, searchItem);
        } catch (ItemException e) {
            throw new ReviewException(e.getMessage());
        }

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @DeleteMapping("/deleteReview")
    public ResponseEntity<HttpStatus> deleteReview(@RequestBody Map<String, String> item) {
        Item searchItem = responseHelper(item);
        itemReviewsService.deleteReview(searchItem);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/deleteReviewByAdmin")
    public ResponseEntity<HttpStatus> deleteReviewByAdmin(@RequestBody @Valid SearchReviewDTO reviewDTO,
                                                          BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.REVIEW_EXCEPTION);
        try {
            itemReviewsService.deleteUserReview(itemService.getItemByName(reviewDTO.getItemName()), reviewDTO.getUsername());
        } catch (ItemException e) {
            throw new ReviewException(e.getMessage());
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PatchMapping("/addLike")
    public ResponseEntity<HttpStatus> addLike(@RequestBody @Valid SearchReviewDTO reviewDTO,
                                              BindingResult bindingResult) {
         ErrorHandler.handleException(bindingResult, ExceptionType.REVIEW_EXCEPTION);
         try {
             itemReviewsService.addLikeToReview(itemService.getItemByName(reviewDTO.getItemName()), reviewDTO.getUsername());
         }catch (ItemException e) {
             throw new ReviewException(e.getMessage());
         }
         return ResponseEntity.ok(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/getUserReviewsForAdmin")
    public List<ResponseItemReviewsDTO> getAllUserReviewsForAdmin(@RequestBody Map<String, String> user) {
        User owner = userService.getUserByUserName(user.get("username"))
                .orElseThrow(() -> new ReviewException("User not found"));
        return itemReviewsService.getAllUserReviewsForAdmin(owner);
    }

    @GetMapping("/getUserReviews")
    public List<ResponseItemReviewsDTO> getUserReviews() {
        return itemReviewsService.getAllUserReviews();
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(ReviewException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private Item responseHelper(Map<String, String> item) {
        if (item.get("itemName") == null) {
            throw new ReviewException("Incorrect key");
        }
        Item item1;
        try {
            item1 = itemService.getItemByName(item.get("itemName"));
        }catch (ItemException e) {
            throw new ReviewException(e.getMessage());
        }
        return item1;
    }
}

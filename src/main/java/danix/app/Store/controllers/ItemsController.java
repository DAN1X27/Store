package danix.app.Store.controllers;

import danix.app.Store.dto.*;
import danix.app.Store.models.CategoryType;
import danix.app.Store.models.Item;
import danix.app.Store.repositories.ItemsRepository;
import danix.app.Store.services.ItemService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemsController {
    private final ItemService itemService;
    private final ItemValidator itemValidator;
    private final ItemsRepository itemsRepository;

    @GetMapping
    public List<ResponseItemDTO> getAllItems(@RequestParam(value = "sort-by-price", required = false) boolean sortByPrice,
                                             @RequestParam(value = "sort-by-rating", required = false) boolean sortByRating,
                                             @RequestParam int page, @RequestParam int count,
                                             @RequestParam(defaultValue = "NONE") CategoryType category) {
        if(sortByPrice) {
            return itemService.getAllItemsSortedByPrice(category, page, count);
        } else if (sortByRating) {
            return itemService.getAllSortedByRating(category, page, count);
        }
        return itemService.getAllItems(category, page, count, "id");
    }

    @GetMapping("/find")
    public FindItemDTO findItem(@RequestBody Map<String, String> item) {
        requestHelper(item);
        return itemService.findItemByName(item.get("name"));
    }

    @PostMapping("/grade")
    public ResponseEntity<String> addGrade(@RequestBody @Valid AddGradeDTO grade,
                                           BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.ITEM_EXCEPTION);
        Item item = itemService.getItemByName(grade.getItemName());
        itemService.addGradeToItem(grade.getGrade(), item);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/new")
    public ResponseEntity<HttpStatus> addItem(@RequestBody @Valid SaveItemDTO item,
                                                       BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.ITEM_EXCEPTION);
        itemService.addItem(item);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}/add")
    public ResponseEntity<HttpStatus> addItem(@PathVariable int id) {
        itemService.addItem(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{id}/image")
    public ResponseEntity<HttpStatus> addImage(@PathVariable int id, @RequestParam("image") MultipartFile file) {
        itemService.addImage(file, id);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<?> getItemImage(@PathVariable long id) {
        ResponseImageDTO image = itemService.getImage(id);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(image.getMediaType())
                .body(image.getImageData());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/image/{id}")
    public ResponseEntity<HttpStatus> deleteImage(@PathVariable long id) {
        itemService.deleteImage(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteItem(@PathVariable int id, @RequestParam(value = "count", required = false) Integer count) {
        if (count != null && count < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        itemService.deleteItem(id, count);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping
    public ResponseEntity<String> updateItem(@RequestBody @Valid UpdateItemDTO updateItemDTO,
                                             BindingResult bindingResult) {
        itemValidator.validate(updateItemDTO.getName(), bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.ITEM_EXCEPTION);
        itemsRepository.findByName(updateItemDTO.getSaveItem().getName()).ifPresent(item -> {
            throw new ItemException("Item " + updateItemDTO.getSaveItem().getName() + "already exists");
        });
        itemService.updateItem(itemService.getItemByName(updateItemDTO.getName()).getId(), updateItemDTO.getSaveItem());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(ItemException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(ImageException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private void requestHelper(Map<String, String> item) {
        if (!item.containsKey("name")) {
            throw new ItemException("Incorrect key");

        }else if (itemsRepository.findByName(item.get("name")).isEmpty()) {
            throw new ItemException("Item not found");
        }
    }
}

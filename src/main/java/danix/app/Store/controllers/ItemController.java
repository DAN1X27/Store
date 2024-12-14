package danix.app.Store.controllers;

import danix.app.Store.dto.*;
import danix.app.Store.models.CategoryType;
import danix.app.Store.models.Item;
import danix.app.Store.repositories.ItemRepository;
import danix.app.Store.services.ItemService;
import danix.app.Store.util.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemValidator itemValidator;
    private final ItemRepository itemRepository;

    @GetMapping("/getAll")
    public List<ResponseItemDTO> getAllItems(@RequestParam(value = "sort-by-price", required = false) boolean sort,
                                             @RequestParam(value = "sort-by-rating", required = false) boolean sortByRating,
                                             @RequestBody Map<String, String> category) {
        String categoryName = category.get("category");
        if (categoryName == null) {
            throw new ItemException("Incorrect category");
        }

        if(sort) {
            return itemService.getAllItemsSortedByPrice(categoryName);
        } else if (sortByRating) {
            return itemService.getAllSortedByRating(categoryName);
        }
        return itemService.getAllItems(categoryName);
    }

    @GetMapping("/findItem")
    public FindItemDTO findItem(@RequestBody Map<String, String> item) {
        requestHelper(item);
        return itemService.findItemByName(item.get("name"));
    }

    @PostMapping("/addGrade")
    public ResponseEntity<String> addGrade(@RequestBody @Valid AddGradeDTO grade,
                                           BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.ITEM_EXCEPTION);
        Item item = itemService.getItemByName(grade.getItemName());
        itemService.addGradeToItem(grade.getGrade(), item);
        return ResponseEntity.ok("Grade successfully added");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<HttpStatus> addItem(@RequestBody @Valid SaveItemDTO item,
                                                       BindingResult bindingResult) {
        ErrorHandler.handleException(bindingResult, ExceptionType.ITEM_EXCEPTION);
        itemService.addItem(item);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/add/{id}")
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
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteItem(@PathVariable int id, @RequestParam(value = "count", required = false) Integer count) {
        if (count != null && count < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        itemService.deleteItem(id, count);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping()
    public ResponseEntity<String> updateItem(@RequestBody @Valid UpdateItemDTO updateItemDTO,
                                                          BindingResult bindingResult) {
        itemValidator.validate(updateItemDTO.getName(), bindingResult);
        ErrorHandler.handleException(bindingResult, ExceptionType.ITEM_EXCEPTION);
        itemRepository.findByName(updateItemDTO.getSaveItem().getName()).ifPresent(item -> {
            throw new ItemException("Item " + updateItemDTO.getSaveItem().getName() + "already exists");
        });
        itemService.updateItem(itemService.getItemByName(updateItemDTO.getName()).getId(), updateItemDTO.getSaveItem());
        return ResponseEntity.ok().body("Updated successfully " + updateItemDTO.getName() + " to " + updateItemDTO.getSaveItem());
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

        }else if (itemRepository.findByName(item.get("name")).isEmpty()) {
            throw new ItemException("Item not found");
        }
    }
}

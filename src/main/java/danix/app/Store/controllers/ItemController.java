package danix.app.Store.controllers;

import danix.app.Store.dto.DeleteItemDTO;
import danix.app.Store.dto.SaveItemDTO;
import danix.app.Store.dto.UpdateItemDTO;
import danix.app.Store.models.Item;
import danix.app.Store.services.ItemService;
import danix.app.Store.util.ErrorResponse;
import danix.app.Store.util.ItemErrorHandler;
import danix.app.Store.util.ItemException;
import danix.app.Store.util.ItemValidator;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    private final ModelMapper modelMapper;

    private final ItemValidator itemValidator;

    @Autowired
    public ItemController(ItemService itemService, ModelMapper modelMapper, ItemValidator itemValidator) {
        this.itemService = itemService;
        this.modelMapper = modelMapper;
        this.itemValidator = itemValidator;
    }

    @GetMapping("/getAll")
    public List<SaveItemDTO> getAllItems(@RequestParam(value = "sort", required = false) boolean sort) {

        if(sort) {
            return itemService.getAllItemsSortedByPrice().stream().map(this::convertToItemDTO).collect(Collectors.toList());
        }

        return itemService.getALlItems().stream().map(this::convertToItemDTO).collect(Collectors.toList());
    }

    @GetMapping("/findItem")
    public SaveItemDTO findItem(@RequestBody Map<String, String> item) {

        requestHelper(item);

        SaveItemDTO requestItem = convertToItemDTO(itemService.findItemByName(item.get("name")).get());

        return requestItem;
    }

    @PostMapping("/addItem")
    public ResponseEntity<String> addItem(@RequestBody @Valid SaveItemDTO item,
                                                       BindingResult bindingResult) {

        ItemErrorHandler.handleException(bindingResult);

        itemService.addItem(convertToItem(item));

        return ResponseEntity.ok("Item added: " + item.getName());
    }

    @PatchMapping("/deleteItem")
    public ResponseEntity<String> deleteItem(@RequestBody @Valid DeleteItemDTO item,
                                                          BindingResult bindingResult) {

        itemValidator.validate(item.getName(), bindingResult);

        ItemErrorHandler.handleException(bindingResult);

        itemService.deleteItem(convertToItem(item));

        return ResponseEntity.ok("Deleted successfully for item: " + item.getName());
    }

    @PatchMapping("/updateItem")
    public ResponseEntity<String> updateItem(@RequestBody @Valid UpdateItemDTO updateItemDTO,
                                                          BindingResult bindingResult1, BindingResult bindingResult2) {

        itemValidator.validate(updateItemDTO.getName(), bindingResult1);
        ItemErrorHandler.handleException(bindingResult1);

        Validator updatedItemValidator = new Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return String.class.equals(clazz);
            }

            @Override
            public void validate(Object target, Errors errors) {
                String itemName = (String) target;

                if(itemService.findItemByName(itemName).isPresent()) {
                    errors.rejectValue("saveItem", "", "Item with this name already exist.");
                }
            }
        };

        updatedItemValidator.validate(updateItemDTO.getSaveItem().getName(), bindingResult2);
        ItemErrorHandler.handleException(bindingResult2);

        itemService.updateItem(itemService.findItemByName(updateItemDTO.getName()).get().getId(),
                convertToItem(updateItemDTO.getSaveItem()));

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

    private void requestHelper(Map<String, String> item) {
        if(!item.containsKey("name")) {
            throw new ItemException("Incorrect key");

        }else if(itemService.findItemByName(item.get("name")).isEmpty()) {
            throw new ItemException("Item not found");
        }
    }

    private Item convertToItem(SaveItemDTO saveItemDTO) {
        return modelMapper.map(saveItemDTO, Item.class);
    }

    private Item convertToItem(DeleteItemDTO deleteItemDTO) {
        return modelMapper.map(deleteItemDTO, Item.class);
    }

    private SaveItemDTO convertToItemDTO(Item item) {
        return modelMapper.map(item, SaveItemDTO.class);
    }
}

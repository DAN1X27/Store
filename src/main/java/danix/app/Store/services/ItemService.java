package danix.app.Store.services;

import danix.app.Store.dto.FindItemDTO;
import danix.app.Store.dto.ItemDTO;
import danix.app.Store.dto.ResponseItemDTO;
import danix.app.Store.dto.SaveItemDTO;
import danix.app.Store.models.CategoryType;
import danix.app.Store.models.Item;
import danix.app.Store.models.ItemGrade;
import danix.app.Store.repositories.ItemRepository;
import danix.app.Store.repositories.ItemsGradesRepository;
import danix.app.Store.util.ItemException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;
    private final ItemReviewsService itemReviewsService;
    private final ItemsGradesService itemGradesService;

    @Autowired
    public ItemService(ItemRepository itemRepository, ModelMapper modelMapper,
                       ItemReviewsService itemReviewsService, ItemsGradesService itemGradesService) {
        this.itemRepository = itemRepository;
        this.modelMapper = modelMapper;
        this.itemReviewsService = itemReviewsService;
        this.itemGradesService = itemGradesService;
    }

    public List<ResponseItemDTO> getAllItems(String category) {
        CategoryType categoryType;
        try {
            categoryType = CategoryType.valueOf(category.toUpperCase());
        }catch (IllegalArgumentException e) {
            throw new ItemException("Illegal category");
        }
        if (categoryType == CategoryType.NONE) {
            return itemRepository.findAll().stream()
                    .map(this::convertToResponseItemDTO).toList();
        }
        return itemRepository.findAll().stream()
                .map(this::convertToResponseItemDTO)
                .filter(item -> item.getCategoryType() == categoryType)
                .collect(Collectors.toList());
    }

    public FindItemDTO findItemByName(String name) {
        return convertToFindItemDTO(itemRepository.findByName(name)
                .orElseThrow(() ->  new ItemException("Item not found")));
    }

    public Item getItemByName(String name) {
        return itemRepository.findByName(name).orElseThrow(() -> new ItemException("Item not found"));
    }

    public List<ResponseItemDTO> getAllSortedByRating(String category) {
        return getAllItems(category).stream()
                .sorted(Comparator.comparing(ResponseItemDTO::getRating)).toList();
    }

    public List<ResponseItemDTO> getAllItemsSortedByPrice(String category) {
        return getAllItems(category).stream()
                .sorted(Comparator.comparing(ResponseItemDTO::getPrice)).toList();
    }

    @Transactional
    public void addGradeToItem(int grade, Item item) {
        itemGradesService.getByItemAndOwner(item, PersonService.getCurrentUser()).ifPresentOrElse(itemGrade -> {
            itemGrade.setGrade(grade);
        }, () -> {
            itemGradesService.addItemGrade(new ItemGrade(item, grade, PersonService.getCurrentUser()));
        });
        List<ItemGrade> itemGrades = itemGradesService.getAllByItem(item);
        int sum = itemGrades.stream().mapToInt(ItemGrade::getGrade).sum();
        double rating = Math.round((double) sum / itemGrades.size() * 10.0) / 10.0;
        item.setRating(rating);
    }

    @Transactional
    public void addItem(SaveItemDTO saveItemDTO) {
        itemRepository.findByName(saveItemDTO.getName()).ifPresentOrElse(item -> {
            item.setCount(item.getCount() + saveItemDTO.getCount());
        }, () -> {
            Item item = convertToItem(saveItemDTO);
            item.setRating(0.0);
            itemRepository.save(item);
        });
    }

    @Transactional
    public void deleteItem(ItemDTO item) {
        if (item.getCount() == 0) {
            itemRepository.delete(convertToItem(item));
        }else {
            Optional<Item> item1 = itemRepository.findByName(item.getName());
            Integer count = item1.get().getCount();
            item1.get().setCount(--count);
        }
    }

    @Transactional
    public void updateItem(Integer id ,SaveItemDTO saveItemDTO) {
        Item item = convertToItem(saveItemDTO);
        item.setId(id);
        itemRepository.save(item);
    }

    private FindItemDTO convertToFindItemDTO(Item item) {
        FindItemDTO findItemDTO = new FindItemDTO();
        findItemDTO.setName(item.getName());
        findItemDTO.setCount(item.getCount());
        findItemDTO.setPrice(item.getPrice());
        findItemDTO.setReviews(item.getReviews().stream()
                .map(itemReviewsService::convertToItemReviewsDTO).toList());
        findItemDTO.setCategory(item.getCategory().toString());
        findItemDTO.setDescription(item.getDescription());
        itemGradesService.getByItemAndOwner(item, PersonService.getCurrentUser())
                .ifPresentOrElse(itemGrade -> {
                    findItemDTO.setUserGrade(itemGrade.getGrade());
                }, () -> {
                    findItemDTO.setUserGrade(null);
                });
        findItemDTO.setRating(item.getRating() == 0.0 ? null : item.getRating());
        return findItemDTO;
    }

    private Item convertToItem(SaveItemDTO saveItemDTO) {
        Item item = new Item();
        try {
            item.setCategory(CategoryType.valueOf(saveItemDTO.getCategory().toUpperCase()));
        } catch (IllegalArgumentException e){
            throw new ItemException("Invalid category");
        }
        item.setPrice(saveItemDTO.getPrice());
        item.setCount(saveItemDTO.getCount());
        item.setName(saveItemDTO.getName());
        item.setDescription(saveItemDTO.getDescription());
        return item;
    }

    private Item convertToItem(ItemDTO itemDTO) {
        return modelMapper.map(itemDTO, Item.class);
    }

    public ResponseItemDTO convertToResponseItemDTO(Item item) {
        ResponseItemDTO responseItemDTO = new ResponseItemDTO();
        responseItemDTO.setName(item.getName());
        responseItemDTO.setPrice(item.getPrice());
        responseItemDTO.setCount(item.getCount());
        responseItemDTO.setReviewsCount(item.getReviews().size());
        responseItemDTO.setCategoryType(item.getCategory());
        responseItemDTO.setRating(item.getRating());
        return responseItemDTO;
    }
}

package danix.app.Store.services;

import danix.app.Store.dto.*;
import danix.app.Store.models.CategoryType;
import danix.app.Store.models.Item;
import danix.app.Store.models.ItemGrade;
import danix.app.Store.models.ItemImage;
import danix.app.Store.repositories.ItemRepository;
import danix.app.Store.repositories.ItemsImagesRepository;
import danix.app.Store.util.ImageException;
import danix.app.Store.util.ItemException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemReviewsService itemReviewsService;
    private final ItemsGradesService itemGradesService;
    private final ItemsImagesRepository imagesRepository;

    @Value("${items_images_path}")
    private String IMAGES_PATH;

    public List<ResponseItemDTO> getAllItems(String category) {
        CategoryType categoryType;
        try {
            categoryType = CategoryType.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
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
                .orElseThrow(() -> new ItemException("Item not found")));
    }

    public Item getItemByName(String name) {
        return itemRepository.findByName(name)
                .orElseThrow(() -> new ItemException("Item not found"));
    }

    public List<ResponseItemDTO> getAllSortedByRating(String category) {
        return getAllItems(category).stream()
                .sorted(Comparator.comparing(ResponseItemDTO::getRating))
                .toList();
    }

    public List<ResponseItemDTO> getAllItemsSortedByPrice(String category) {
        return getAllItems(category).stream()
                .sorted(Comparator.comparing(ResponseItemDTO::getPrice))
                .toList();
    }

    @Transactional
    public void addGradeToItem(int grade, Item item) {
        itemGradesService.getByItemAndOwner(item, UserService.getCurrentUser()).ifPresentOrElse(itemGrade -> {
            itemGrade.setGrade(grade);
        }, () -> {
            itemGradesService.addItemGrade(new ItemGrade(item, grade, UserService.getCurrentUser()));
        });
        List<ItemGrade> itemGrades = itemGradesService.getAllByItem(item);
        int sum = itemGrades.stream().mapToInt(ItemGrade::getGrade).sum();
        double rating = Math.round((double) sum / itemGrades.size() * 10.0) / 10.0;
        item.setRating(rating);
    }

    @Transactional
    public void addItem(SaveItemDTO saveItemDTO) {
        itemRepository.findByName(saveItemDTO.getName()).ifPresentOrElse(item -> {
            throw new ItemException("Item with the same name already exists");
        }, () -> {
            Item item = convertToItem(saveItemDTO);
            item.setRating(0.0);
            itemRepository.save(item);
        });
    }

    @Transactional
    public void addItem(int id) {
        Item item = itemRepository.findById(id)
                        .orElseThrow(() -> new ItemException("Item not found"));
        item.setCount(item.getCount() + 1);
    }

    @Transactional
    public void deleteItem(int itemId, Integer itemsCount) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException("Item not found"));
        if (itemsCount == null || itemsCount > item.getCount()) {
            item.getImages().forEach(image -> {
                try {
                    Files.deleteIfExists(Path.of(IMAGES_PATH, image.getImageUUID() + ".png"));
                    Files.deleteIfExists(Path.of(IMAGES_PATH, image.getImageUUID() + ".jpg"));
                } catch (IOException e) {
                    throw new ItemException("Error when delete item");
                }
            });
            itemRepository.delete(item);
        } else {
            item.setCount(item.getCount() - itemsCount);
        }
    }

    @Transactional
    public void updateItem(Integer id, SaveItemDTO saveItemDTO) {
        Item item = convertToItem(saveItemDTO);
        item.setId(id);
        itemRepository.save(item);
    }

    @Transactional
    public void addImage(MultipartFile image, int id) {
        if (!Objects.requireNonNull(image.getOriginalFilename()).endsWith(".jpg") &&
                !Objects.requireNonNull(image.getOriginalFilename()).endsWith(".png")) {
            throw new ItemException("Invalid image type");
        }
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemException("Item not found"));
        if (item.getImages().size() >= 5) {
            throw new ItemException("Too many images");
        }
        Path path = Path.of(IMAGES_PATH);
        String uuid = UUID.randomUUID().toString();
        File file = new File(path.toString(), uuid + (Objects.requireNonNull(image.getOriginalFilename()).endsWith(".jpg") ? ".jpg" : ".png"));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(image.getBytes());
        } catch (IOException e) {
            throw new ImageException("Error when upload image");
        }
        ItemImage itemImage = new ItemImage();
        itemImage.setItem(item);
        itemImage.setImageUUID(uuid);
        imagesRepository.save(itemImage);
    }

    public ResponseImageDTO getImage(long id) {
        ItemImage itemImage = imagesRepository.findById(id)
                .orElseThrow(() -> new ImageException("Image not found"));
        Path path = Path.of(IMAGES_PATH);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString().substring(0, file.getFileName().toString().lastIndexOf("."));
                if (itemImage.getImageUUID().equals(fileName)) {
                    byte[] imageData = Files.readAllBytes(file);
                    return new ResponseImageDTO(imageData, file.getFileName().endsWith(".jpg") ? MediaType.IMAGE_JPEG : MediaType.IMAGE_PNG);
                }
            }
        } catch (IOException e) {
            throw new ImageException("Error when download image");
        }
        throw new ItemException("Image not found");
    }

    @Transactional
    public void deleteImage(long id) {
        ItemImage image = imagesRepository.findById(id)
                .orElseThrow(() -> new ImageException("Image not found"));
        try {
            Files.deleteIfExists(Path.of(IMAGES_PATH, image.getImageUUID() + ".jpg"));
            Files.deleteIfExists(Path.of(IMAGES_PATH, image.getImageUUID() + ".png"));
        } catch (IOException e) {
            throw new ImageException("Error when delete image");
        }
        imagesRepository.delete(image);
    }

    private FindItemDTO convertToFindItemDTO(Item item) {
        return FindItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .count(item.getCount())
                .price(item.getPrice())
                .reviews(item.getReviews().stream()
                        .map(itemReviewsService::convertToItemReviewsDTO)
                        .toList())
                .category(item.getCategory())
                .description(item.getDescription())
                .images(item.getImages().stream()
                        .map(image -> new ItemImageIdDTO(image.getId()))
                        .toList())
                .userGrade(itemGradesService.getByItemAndOwner(item, UserService.getCurrentUser()).isPresent() ?
                        itemGradesService.getByItemAndOwner(item, UserService.getCurrentUser()).get().getGrade() : null)
                .rating(item.getRating() == 0.0 ? null : item.getRating())
                .build();
    }

    private Item convertToItem(SaveItemDTO saveItemDTO) {
        Item item = new Item();
        try {
            item.setCategory(CategoryType.valueOf(saveItemDTO.getCategory().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ItemException("Invalid category");
        }
        item.setPrice(saveItemDTO.getPrice());
        item.setCount(saveItemDTO.getCount());
        item.setName(saveItemDTO.getName());
        item.setDescription(saveItemDTO.getDescription());
        return item;
    }

    public ResponseItemDTO convertToResponseItemDTO(Item item) {
        ResponseItemDTO responseItemDTO = new ResponseItemDTO();
        responseItemDTO.setName(item.getName());
        responseItemDTO.setPrice(item.getPrice());
        responseItemDTO.setCount(item.getCount());
        responseItemDTO.setReviewsCount(item.getReviews().size());
        responseItemDTO.setCategoryType(item.getCategory());
        responseItemDTO.setRating(item.getRating());
        responseItemDTO.setId(item.getId());
        return responseItemDTO;
    }
}

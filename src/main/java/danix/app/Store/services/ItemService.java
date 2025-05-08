package danix.app.Store.services;

import danix.app.Store.dto.*;
import danix.app.Store.models.*;
import danix.app.Store.repositories.ItemsGradesRepository;
import danix.app.Store.repositories.ItemsRepository;
import danix.app.Store.repositories.ItemsImagesRepository;
import danix.app.Store.util.ImageException;
import danix.app.Store.util.ItemException;
import danix.app.Store.util.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static danix.app.Store.services.UserService.getCurrentUser;
import static danix.app.Store.util.PageUtils.getPage;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemsRepository itemsRepository;
    private final ItemReviewService itemReviewService;
    private final ItemsImagesRepository imagesRepository;
    private final ItemsGradesRepository gradesRepository;
    private final ModelMapper modelMapper;

    @Value("${items_images_path}")
    private String IMAGES_PATH;

    public List<ResponseItemDTO> getAllItems(CategoryType category, int page, int count, String sortProperty) {
        if (category == CategoryType.NONE) {
            return itemsRepository.findAll(getPage(page, count, sortProperty)).stream()
                    .map(this::convertToResponseItemDTO)
                    .toList();
        }
        return itemsRepository.findAllByCategory(category, getPage(page, count, sortProperty)).stream()
                .map(this::convertToResponseItemDTO)
                .toList();
    }

    public FindItemDTO findItemByName(String name) {
        return convertToFindItemDTO(itemsRepository.findByName(name)
                .orElseThrow(() -> new ItemException("Item not found")));
    }

    public Item getItemByName(String name) {
        return itemsRepository.findByName(name)
                .orElseThrow(() -> new ItemException("Item not found"));
    }

    public List<ResponseItemDTO> getAllSortedByRating(CategoryType category, int page, int count) {
        return getAllItems(category, page, count, "rating");
    }

    public List<ResponseItemDTO> getAllItemsSortedByPrice(CategoryType category, int page, int count) {
        return getAllItems(category, page, count, "price");
    }

    @Transactional
    public void addGradeToItem(int grade, Item item) {
        User currentUser = getCurrentUser();
        gradesRepository.findByItemAndOwner(item, currentUser)
                .ifPresentOrElse(itemGrade -> itemGrade.setGrade(grade),
                () -> gradesRepository.save(new ItemGrade(item, grade, currentUser)));
        List<ItemGrade> itemGrades = gradesRepository.findAllByItem(item);
        double rating = itemGrades.stream().mapToDouble(ItemGrade::getGrade).sum() / itemGrades.size();
        item.setRating(rating);
    }

    @Transactional
    public void addItem(SaveItemDTO saveItemDTO) {
        itemsRepository.findByName(saveItemDTO.getName()).ifPresentOrElse(item -> {
            throw new ItemException("Item with the same name already exists");
        }, () -> {
            Item item = modelMapper.map(saveItemDTO, Item.class);
            item.setRating(0.0);
            itemsRepository.save(item);
        });
    }

    @Transactional
    public void addItem(int id) {
        Item item = itemsRepository.findById(id)
                        .orElseThrow(() -> new ItemException("Item not found"));
        item.setCount(item.getCount() + 1);
    }

    @Transactional
    public void deleteItem(int itemId, Integer itemsCount) {
        Item item = itemsRepository.findById(itemId)
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
            itemsRepository.delete(item);
        } else {
            item.setCount(item.getCount() - itemsCount);
        }
    }

    @Transactional
    public void updateItem(Integer id, SaveItemDTO saveItemDTO) {
        Item item = modelMapper.map(saveItemDTO, Item.class);
        item.setId(id);
        itemsRepository.save(item);
    }

    @Transactional
    public void addImage(MultipartFile image, int id) {
        String name = Objects.requireNonNull(image.getOriginalFilename());
        if (!name.endsWith(".jpg") && !name.endsWith(".png")) {
            throw new ItemException("Invalid file type");
        }
        Item item = itemsRepository.findById(id)
                .orElseThrow(() -> new ItemException("Item not found"));
        if (item.getImages().size() >= 5) {
            throw new ItemException("Too many images");
        }
        Path path = Path.of(IMAGES_PATH);
        String uuid = UUID.randomUUID().toString();
        File file = new File(path.toString(), uuid + (name.endsWith(".jpg") ? ".jpg" : ".png"));
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(image.getBytes());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ImageException("Error uploading image");
        }
        ItemImage itemImage = new ItemImage();
        itemImage.setItem(item);
        itemImage.setImageUUID(uuid);
        imagesRepository.save(itemImage);
    }

    public ResponseImageDTO getImage(long id) {
        ItemImage itemImage = imagesRepository.findById(id)
                .orElseThrow(() -> new ImageException("Image not found"));
        Path path = Path.of(IMAGES_PATH, itemImage.getImageUUID() + ".jpg");
        if (!Files.exists(path)) {
            path = Path.of(IMAGES_PATH, itemImage.getImageUUID() + ".png");
            if (!Files.exists(path)) {
                throw new ItemException("Image not found");
            }
        }
        try {
            byte[] data = Files.readAllBytes(path);
            return ResponseImageDTO.builder()
                    .imageData(data)
                    .mediaType(MediaType.IMAGE_JPEG)
                    .build();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ItemException("Error downloading image");
        }
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
                        .map(itemReviewService::convertToItemReviewDTO)
                        .toList())
                .category(item.getCategory())
                .description(item.getDescription())
                .images(item.getImages().stream()
                        .map(image -> new ItemImageIdDTO(image.getId()))
                        .toList())
                .userGrade(gradesRepository.findByItemAndOwner(item, getCurrentUser()).isPresent() ?
                        gradesRepository.findByItemAndOwner(item, getCurrentUser()).get().getGrade() : null)
                .rating(item.getRating() == 0.0 ? null : item.getRating())
                .build();
    }

    public ResponseItemDTO convertToResponseItemDTO(Item item) {
        ResponseItemDTO itemDTO = modelMapper.map(item, ResponseItemDTO.class);
        itemDTO.setReviewsCount(item.getReviews().size());
        return itemDTO;
    }
}

package danix.app.Store.services;

import danix.app.Store.dto.*;
import danix.app.Store.models.Item;
import danix.app.Store.models.Order;
import danix.app.Store.models.OrderedItems;
import danix.app.Store.models.User;
import danix.app.Store.repositories.IdProjection;
import danix.app.Store.repositories.OrdersRepository;
import danix.app.Store.repositories.OrderedItemsRepository;
import danix.app.Store.util.OrderException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static danix.app.Store.services.UserService.getCurrentUser;
import static danix.app.Store.util.PageUtils.getPage;
import static danix.app.Store.util.PageUtils.getSort;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrdersRepository ordersRepository;
    private final ItemService itemService;
    private final ModelMapper modelMapper;
    private final OrderedItemsRepository orderedItemsRepository;
    private final UserService userService;

    @Transactional
    public List<ResponseAdminOrderDTO> getAllOrders(int page, int count) {
        List<Integer> ids = convertToIds(ordersRepository.findAllOrders(getPage(page, count, "id")));
        return ordersRepository.findAllByIdIn(ids, getSort("id")).stream()
                .map(this::convertToAdminOrderDTO)
                .toList();
    }

    @Transactional
    public List<ResponseAdminOrderDTO> getAllUserOrdersForAdmin(String userName, int page, int count) {
        User user = userService.getUserByUserName(userName)
                .orElseThrow(() -> new OrderException("User not found"));
        List<Integer> ids = convertToIds(ordersRepository.findAllByOwner(user, getPage(page, count, "id")));
        return ordersRepository.findAllByIdIn(ids, getSort("id")).stream()
                .map(this::convertToAdminOrderDTO)
                .toList();
    }

    @Transactional
    public List<ResponseOrderDTO> getAllUserOrders(int page, int count) {
        User owner = getCurrentUser();
        List<Integer> ids = convertToIds(ordersRepository.findAllByOwner(owner, getPage(page, count, "id")));
        return ordersRepository.findAllByIdIn(ids, getSort("id")).stream()
                .map(this::convertToOrderDTO)
                .toList();
    }

    @Transactional
    public void takeOrder(int id) {
        Order order = getById(id);
        checkOrderOwner(order);
        if (!order.isReady()) {
            throw new OrderException("This order is not ready yet");
        }
        ordersRepository.delete(order);
    }

    @Transactional
    public void cancelOrder(int id) {
        Order order = getById(id);
        checkOrderOwner(order);
        returnItemsCount(order);
        ordersRepository.delete(order);
    }

    @Transactional
    public void cancelOrderByAdmin(int id) {
        Order order = getById(id);
        returnItemsCount(order);
        ordersRepository.delete(order);
    }

    private void returnItemsCount(Order order) {
        order.getItems().forEach(item -> item.setCount(item.getCount() + 1));
    }

    @Transactional
    public void createOrder(OrderDTO orderDTO) {
        User currentUser = getCurrentUser();
        Calendar readyDate = Calendar.getInstance();
        readyDate.add(Calendar.DAY_OF_MONTH, 2);
        Calendar storageDate = Calendar.getInstance();
        storageDate.add(Calendar.DAY_OF_MONTH, 16);
        Order order = Order.builder()
                .storageDate(storageDate.getTime())
                .orderReadyDate(readyDate.getTime())
                .createdAt(LocalDateTime.now())
                .isReady(false)
                .owner(currentUser)
                .price(orderDTO.getItems().stream()
                        .mapToDouble(item -> item.getCount() * itemService.getItemByName(item.getName()).getPrice())
                        .sum())
                .build();
        order.setItems(getItems(orderDTO, order));
        ordersRepository.save(order);
    }

    private List<Item> getItems(OrderDTO orderDTO, Order order) {
        Map<String, ItemDTO> itemDTOs = orderDTO.getItems().stream()
                .collect(Collectors.toMap(ItemDTO::getName, Function.identity()));

        return orderDTO.getItems().stream()
                .map(item -> itemService.getItemByName(item.getName()))
                .map(item -> setCount(item, itemDTOs, order))
                .collect(Collectors.toList());
    }

    private Item setCount(Item item, Map<String, ItemDTO> itemDTOs, Order order) {
        ItemDTO itemDTO = itemDTOs.get(item.getName());
        if(itemDTO != null) {
            item.setCount(item.getCount() - itemDTO.getCount());
            OrderedItems orderedItems = new OrderedItems(order, item, itemDTO.getCount());
            orderedItemsRepository.save(orderedItems);
        }
        return item;
    }

    public ResponseOrderDTO convertToOrderDTO(Order order) {
        ResponseOrderDTO responseOrderDTO = modelMapper.map(order, ResponseOrderDTO.class);
        responseOrderDTO.setItems(getItems(order));
        return responseOrderDTO;
    }

    private List<SaveItemDTO> getItems(Order order) {
        Map<String, OrderedItems> orderedItems = orderedItemsRepository.findAllByOrder(order).stream()
                .collect(Collectors.toMap(OrderedItems::getItemName, Function.identity()));
        return order.getItems().stream()
                .map(item -> modelMapper.map(item, SaveItemDTO.class))
                .map(item -> setPrice(item, orderedItems))
                .collect(Collectors.toList());
    }

    private SaveItemDTO setPrice(SaveItemDTO saveItemDTO, Map<String, OrderedItems> orderedItems) {
        OrderedItems orderedItem = orderedItems.get(saveItemDTO.getName());
        if(orderedItem != null) {
            saveItemDTO.setPrice(saveItemDTO.getPrice() * orderedItem.getCount());
            saveItemDTO.setCount(orderedItem.getCount());
        }
        return saveItemDTO;
    }

    public ResponseAdminOrderDTO convertToAdminOrderDTO(Order order) {
        ResponseAdminOrderDTO adminOrderDTO = modelMapper.map(order, ResponseAdminOrderDTO.class);
        adminOrderDTO.setItems(getItems(order));
        return adminOrderDTO;
    }

    private List<Integer> convertToIds(List<IdProjection> projections) {
        return projections.stream()
                .map(IdProjection::getId)
                .toList();
    }

    private void checkOrderOwner(Order order) {
        if (!order.getOwner().getId().equals(getCurrentUser().getId())) {
            throw new OrderException("You are not owner of this order");
        }
    }

    private Order getById(int id) {
        return ordersRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found"));
    }
}

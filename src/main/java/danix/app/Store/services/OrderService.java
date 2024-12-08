package danix.app.Store.services;

import danix.app.Store.dto.*;
import danix.app.Store.models.Item;
import danix.app.Store.models.Order;
import danix.app.Store.models.OrderedItems;
import danix.app.Store.models.Person;
import danix.app.Store.repositories.OrderRepository;
import danix.app.Store.util.OrderException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemService itemService;
    private final ModelMapper modelMapper;
    private final OrderedItemsService orderedItemsService;
    private final PersonService personService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ItemService itemService, ModelMapper modelMapper,
                        OrderedItemsService orderedItemsService, PersonService personService) {
        this.orderRepository = orderRepository;
        this.itemService = itemService;
        this.modelMapper = modelMapper;
        this.orderedItemsService = orderedItemsService;
        this.personService = personService;
    }

    @Transactional
    public List<AdminOrderDTO> getAllOrders() {
        return orderRepository.findAllOrders().stream().map(this::convertToAdminOrderDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<AdminOrderDTO> getAllUserOrdersForAdmin(String userName) {
        Person person = personService.getUserByUserName(userName).get();

        return orderRepository.finByOwner(person).stream()
                .map(this::convertToAdminOrderDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<ResponseOrderDTO> getAllUserOrders() {
        Person owner = PersonService.getCurrentUser();

        return orderRepository.finByOwner(owner).stream()
                .map(this::convertToOrderDTO).collect(Collectors.toList());
    }

    @Transactional
    public void takeOrder(int id) {
        Optional<Order> order = orderRepository.findById(id);
        Person owner = PersonService.getCurrentUser();

        if(order.isEmpty()) {
            throw new OrderException("Order with this ID not found.");
        }

        if(!order.get().getOwner().getId().equals(owner.getId())) {
            throw new OrderException("The order belongs to another user");
        }

        if(!order.get().isReady()) {
            throw new OrderException("This order is not ready yet");
        }

        orderRepository.delete(order.get());

    }

    @Transactional
    public void cancelOrder(int id) {
        Optional<Order> order = orderRepository.findById(id);
        Person owner = PersonService.getCurrentUser();

        if (order.isEmpty()) {
            throw new OrderException("Order with this ID not found.");
        }

        if (!order.get().getOwner().getId().equals(owner.getId())) {
            throw new OrderException("The order belongs to another user");
        }

        orderRepository.delete(order.get());
    }

    @Transactional
    public void cancelOrderForAdmin(int id) {
        Optional<Order> order = orderRepository.findById(id);

        if(order.isEmpty()) {
            throw new OrderException("Order with this ID not found.");
        }

        orderRepository.delete(order.get());
    }

    @Transactional
    public void createOrder(OrderDTO order) {
        orderRepository.save(convertToOrder(order));
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
            orderedItemsService.save(orderedItems);
        }
        return item;
    }

    private Order convertToOrder(OrderDTO orderDTO) {
        Order order = new Order();
        Person owner = PersonService.getCurrentUser();

        Calendar readyDate = Calendar.getInstance();
        readyDate.add(Calendar.DAY_OF_MONTH, 2);

        Calendar storageDate = Calendar.getInstance();
        storageDate.add(Calendar.DAY_OF_MONTH, 16);

        order.setStorageDate(storageDate.getTime());
        order.setOrderReadyDate(readyDate.getTime());
        order.setCreatedAt(LocalDateTime.now());
        order.setReady(false);
        order.setOwner(owner);
        order.setItems(getItems(orderDTO, order));
        order.setPrice(orderDTO.getItems().stream().mapToDouble(item -> item.getCount() * itemService.getItemByName(item.getName()).getPrice()).sum());

        return order;
    }

    public ResponseOrderDTO convertToOrderDTO(Order order) {
        ResponseOrderDTO responseOrderDTO = new ResponseOrderDTO();

        responseOrderDTO.setItems(getItems(order));
        responseOrderDTO.setIsReady(order.isReady()
                ? "Order is ready. You can take this." : "The order is not ready yet.");
        responseOrderDTO.setStorageDate(order.getStorageDate());
        responseOrderDTO.setOrderReadyDate(order.getOrderReadyDate());
        responseOrderDTO.setSum(order.getPrice());
        responseOrderDTO.setId(order.getId());

        return responseOrderDTO;
    }

    private List<SaveItemDTO> getItems(Order order) {
        Map<String, OrderedItems> orderedItems = orderedItemsService.getByOrder(order).stream()
                .collect(Collectors.toMap(OrderedItems::getName, Function.identity()));
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

    public AdminOrderDTO convertToAdminOrderDTO(Order order) {
        AdminOrderDTO adminOrderDTO = new AdminOrderDTO();
        adminOrderDTO.setItems(getItems(order));
        adminOrderDTO.setIsReady(order.isReady()
                ? "Order is ready. You can take this." : "The order is not ready yet.");
        adminOrderDTO.setStorageDate(order.getStorageDate());
        adminOrderDTO.setOrderReadyDate(order.getOrderReadyDate());
        adminOrderDTO.setId(order.getId());
        adminOrderDTO.setSum(order.getPrice());
        adminOrderDTO.setCreatedAt(order.getCreatedAt());
        adminOrderDTO.setOwnerName(order.getOwner().getUserName());

        return adminOrderDTO;
    }
}

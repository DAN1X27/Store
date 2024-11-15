package danix.app.Store.services;

import danix.app.Store.dao.OrderDAO;
import danix.app.Store.dto.*;
import danix.app.Store.models.Item;
import danix.app.Store.models.Order;
import danix.app.Store.models.OrderedItems;
import danix.app.Store.models.Person;
import danix.app.Store.repositories.OrderRepository;
import danix.app.Store.security.PersonDetails;
import danix.app.Store.util.OrderErrorHandler;
import danix.app.Store.util.OrderException;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemService itemService;
    private final ModelMapper modelMapper;
    private final OrderedItemsService orderedItemsService;
    private final PersonService personService;
    private final OrderDAO orderDAO;

    @Autowired
    public OrderService(OrderRepository orderRepository, ItemService itemService, ModelMapper modelMapper,
                        OrderedItemsService orderedItemsService, PersonService personService, OrderDAO orderDAO) {
        this.orderRepository = orderRepository;
        this.itemService = itemService;
        this.modelMapper = modelMapper;
        this.orderedItemsService = orderedItemsService;
        this.personService = personService;
        this.orderDAO = orderDAO;
    }

    @Transactional
    public List<AdminOrderDTO> getAllOrders() {
        return orderDAO.getAllOrders().stream().map(this::convertToAdminOrderDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<AdminOrderDTO> getAllUserOrdersForAdmin(String userName) {
        Person person = personService.getUserByUserName(userName).get();

        return orderDAO.findAllByOwner(person).stream()
                .map(this::convertToAdminOrderDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<ResponseOrderDTO> getAllUserOrders() {
        Person owner = PersonService.getCurrentUser();

        return orderDAO.findAllByOwner(owner).stream()
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

    @Transactional
    public void updateOrderReady(Integer id, Order order) {
        order.setId(id);
        orderRepository.save(order);
    }

    private Order convertToOrder(OrderDTO orderDTO) {
        Order order = new Order();

        order.setItems(orderDTO.getItems().stream().map(item ->
                itemService.findItemByName(item.getName()).get()).collect(Collectors.toList()));

        Person owner = PersonService.getCurrentUser();

        double sum = 0;

        for (Item item : order.getItems()) {

            for (ItemDTO itemDTO : orderDTO.getItems()) {
                if (item.getName().equals(itemDTO.getName())) {
                    OrderedItems orderedItems = new OrderedItems(order, item, itemDTO.getCount());
                    orderedItemsService.save(orderedItems);

                    item.setCount(item.getCount() - itemDTO.getCount());
                    sum+=itemDTO.getCount() * item.getPrice();
                    break;
                }
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 2);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.DAY_OF_MONTH, 16);

        order.setStorageDate(calendar1.getTime());
        order.setOrderReadyDate(calendar.getTime());
        order.setCreatedAt(LocalDateTime.now());
        order.setReady(false);
        order.setPrice(sum);
        order.setOwner(owner);

        return order;
    }

    public ResponseOrderDTO convertToOrderDTO(Order order) {
        ResponseOrderDTO responseOrderDTO = new ResponseOrderDTO();

        responseOrderDTO.setItems(order.getItems().stream().map(item ->
                modelMapper.map(item, SaveItemDTO.class)).collect(Collectors.toList()));

        List<OrderedItems> orderedItems = orderedItemsService.getByOrder(order);

        for (SaveItemDTO itemDTO : responseOrderDTO.getItems()) {
            for (OrderedItems orderedItem : orderedItems) {
                if (orderedItem.getItem().getName().equals(itemDTO.getName())) {
                    double price = itemDTO.getPrice() * orderedItem.getCount();
                    itemDTO.setPrice(price);
                    itemDTO.setCount(orderedItem.getCount());
                    break;
                }
            }
        }

        Date date = new Date();
        if(order.getOrderReadyDate().before(date)) {

            if(order.getStorageDate().before(date)) {
                orderRepository.delete(order);

            }else {
                order.setReady(true);
                updateOrderReady(order.getId(), order);
                responseOrderDTO.setIsReady("Order is ready. You can take this.");
            }

        }else {
            responseOrderDTO.setIsReady("The order is not ready yet.");
        }

        responseOrderDTO.setStorageDate(order.getStorageDate());
        responseOrderDTO.setOrderReadyDate(order.getOrderReadyDate());
        responseOrderDTO.setSum(order.getPrice());
        responseOrderDTO.setId(order.getId());

        return responseOrderDTO;
    }

    public AdminOrderDTO convertToAdminOrderDTO(Order order) {
        AdminOrderDTO adminOrderDTO = new AdminOrderDTO();
        adminOrderDTO.setItems(order.getItems().stream().map(item ->
                modelMapper.map(item, SaveItemDTO.class)).collect(Collectors.toList()));

        List<OrderedItems> orderedItems = orderedItemsService.getByOrder(order);

        for (SaveItemDTO itemDTO : adminOrderDTO.getItems()) {

            for (OrderedItems orderedItem : orderedItems) {

                if (orderedItem.getItem().getName().equals(itemDTO.getName())) {
                    double price = orderedItem.getItem().getPrice() * orderedItem.getCount();
                    itemDTO.setPrice(price);
                    itemDTO.setCount(orderedItem.getCount());
                    break;
                }

            }
        }

        Date date = new Date();

        if (order.getOrderReadyDate().before(date)) {

            if(order.getStorageDate().before(date)) {
                orderRepository.delete(order);
            }else {
                order.setReady(true);
                updateOrderReady(order.getId(), order);
                adminOrderDTO.setIsReady("The order is ready.");
            }

        } else {
            adminOrderDTO.setIsReady("The order is not ready yet");
        }

        adminOrderDTO.setStorageDate(order.getStorageDate());
        adminOrderDTO.setOrderReadyDate(order.getOrderReadyDate());
        adminOrderDTO.setId(order.getId());
        adminOrderDTO.setSum(order.getPrice());
        adminOrderDTO.setCreatedAt(order.getCreatedAt());
        adminOrderDTO.setOwnerName(order.getOwner().getUserName());

        return adminOrderDTO;
    }
}

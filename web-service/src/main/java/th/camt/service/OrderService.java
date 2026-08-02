package th.camt.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import th.camt.dto.OrderDTO;
import th.camt.dto.OrderItemDTO;
import th.camt.repository.CustomerRepository;
import th.camt.repository.OrderRepository;
import th.camt.repository.ProductRepository;
import th.mfu.domain.Customer;
import th.mfu.domain.Order;
import th.mfu.domain.OrderItem;
import th.mfu.domain.Product;

// This service is the clearest place to see all 3 relationship types working
// together: create() below pulls in an existing Customer (Many-to-One from
// Order's side) and existing Products (Many-to-One from OrderItem's side),
// builds new OrderItems, and saves everything through ONE orderRepository.save()
// call thanks to cascade=ALL on Order.items (One-to-Many).
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    // @Transactional(readOnly = true) keeps the Hibernate session open while
    // toDTO()/toItemDTO() walk the LAZY order.getItems() -> item.getProduct()
    // chain below; without an open session, touching a lazy association outside
    // a transaction throws LazyInitializationException.
    @Transactional(readOnly = true)
    public List<OrderDTO> list() {
        return orderRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO get(Long id) {
        return toDTO(findOrThrow(id));
    }

    // Demonstrates all three relationships at once:
    //   1. Look up the existing Customer (Many-to-One: Order -> Customer).
    //   2. customer.addOrder(order) links the new Order back to it (One-to-Many).
    //   3. For each requested item, look up the existing Product (Many-to-One:
    //      OrderItem -> Product) and snapshot its current price into unitPrice.
    //   4. order.addItem(item) attaches each OrderItem to the Order.
    // Because Order.items is cascade=ALL, the single orderRepository.save(order)
    // call at the end also inserts every OrderItem - no OrderItemRepository call
    // needed here.
    @Transactional
    public OrderDTO create(OrderDTO dto) {
        if (dto.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Customer " + dto.getCustomerId() + " not found"));

        Order order = new Order();
        order.setOrderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now());
        order.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");
        customer.addOrder(order);

        if (dto.getItems() != null) {
            for (OrderItemDTO itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Product " + itemDto.getProductId() + " not found"));
                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : 1);
                item.setUnitPrice(product.getPrice()); // snapshot price at order time
                order.addItem(item);
            }
        }

        return toDTO(orderRepository.save(order));
    }

    /** Partial update: typically used to change order status, but date can be patched too. */
    @Transactional
    public OrderDTO patch(Long id, OrderDTO dto) {
        Order order = findOrThrow(id);
        if (dto.getStatus() != null) {
            order.setStatus(dto.getStatus());
        }
        if (dto.getOrderDate() != null) {
            order.setOrderDate(dto.getOrderDate());
        }
        return toDTO(orderRepository.save(order));
    }

    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " not found");
        }
        orderRepository.deleteById(id);
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setItems(order.getItems().stream().map(this::toItemDTO).collect(Collectors.toList()));
        return dto;
    }

    private OrderItemDTO toItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        dto.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        return dto;
    }
}

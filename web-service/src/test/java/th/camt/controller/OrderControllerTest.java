package th.camt.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import th.camt.dto.OrderDTO;
import th.camt.dto.OrderItemDTO;
import th.camt.repository.CustomerRepository;
import th.camt.repository.ProductRepository;
import th.mfu.domain.Customer;
import th.mfu.domain.Product;

/**
 * Integration tests for {@link OrderController}. These also exercise the
 * Many-to-One/One-to-Many relationships between Order/Customer and
 * Order/OrderItem/Product.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long createCustomer() {
        Customer customer = new Customer();
        customer.setDisplayname("Order Test Customer");
        customer.setEmail("order.test@example.com");
        customer.setPhone("555-4321");
        return customerRepository.save(customer).getId();
    }

    private Long createProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(9.99);
        product.setDescription("For order tests");
        return productRepository.save(product).getId();
    }

    @Test
    void createOrder_persistsOrderWithItemsLinkedToCustomerAndProduct() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct();

        OrderDTO request = new OrderDTO();
        request.setCustomerId(customerId);
        request.setStatus("PENDING");

        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(productId);
        item.setQuantity(3);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId", is(customerId.intValue())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId", is(productId.intValue())))
                .andExpect(jsonPath("$.items[0].quantity", is(3)));
    }

    @Test
    void listOrders_returnsCreatedOrder() throws Exception {
        Long customerId = createCustomer();

        OrderDTO request = new OrderDTO();
        request.setCustomerId(customerId);
        request.setStatus("PENDING");

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    void patchOrder_updatesStatus() throws Exception {
        Long customerId = createCustomer();

        OrderDTO request = new OrderDTO();
        request.setCustomerId(customerId);
        request.setStatus("PENDING");

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(response).get("id").asLong();

        OrderDTO patchRequest = new OrderDTO();
        patchRequest.setStatus("SHIPPED");

        mockMvc.perform(patch("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHIPPED")));
    }

    @Test
    void deleteOrder_removesOrderAndSubsequentGetReturns404() throws Exception {
        Long customerId = createCustomer();

        OrderDTO request = new OrderDTO();
        request.setCustomerId(customerId);
        request.setStatus("PENDING");

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }
}

package th.camt.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import th.camt.dto.CustomerDTO;
import th.camt.dto.ShippingAddressDTO;
import th.camt.repository.CustomerRepository;
import th.mfu.domain.Customer;

/**
 * Integration tests for {@link CustomerController}.
 *
 * @SpringBootTest boots the real application context (controller, service,
 * repository, H2 database) - not mocks - so this exercises the full stack.
 * @AutoConfigureMockMvc gives us MockMvc, which simulates HTTP requests
 * without starting an actual server/port.
 * @Transactional wraps each test method in a transaction that's rolled back
 * afterwards, so tests don't interfere with each other or depend on the seed
 * data in data.sql (which is disabled for tests - see
 * src/test/resources/application.properties).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void createCustomer_persistsAndReturnsCreatedCustomer() throws Exception {
        CustomerDTO request = new CustomerDTO();
        request.setDisplayname("Alice Wonderland");
        request.setEmail("alice@example.com");
        request.setPhone("555-1234");
        request.setBirthday(LocalDate.of(1995, 6, 1));

        ShippingAddressDTO address = new ShippingAddressDTO();
        address.setAddress("1 Wonderland Way");
        address.setCity("Fantasy City");
        address.setPostalCode("00001");
        address.setCountry("Wonderland");
        request.setShippingAddress(address);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.displayname", is("Alice Wonderland")))
                .andExpect(jsonPath("$.shippingAddress.city", is("Fantasy City")));

        Assertions.assertEquals(1, customerRepository.count());
    }

    @Test
    void listCustomers_returnsAllPersistedCustomers() throws Exception {
        Customer customer = new Customer();
        customer.setDisplayname("Bob Builder");
        customer.setEmail("bob@example.com");
        customer.setPhone("555-5678");
        customer.setBirthday(LocalDate.of(1980, 1, 1));
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].displayname", is("Bob Builder")));
    }

    @Test
    void patchCustomer_updatesOnlyProvidedFields() throws Exception {
        Customer customer = new Customer();
        customer.setDisplayname("Charlie Chaplin");
        customer.setEmail("charlie@example.com");
        customer.setPhone("555-0000");
        customer.setBirthday(LocalDate.of(1970, 5, 5));
        customer = customerRepository.save(customer);

        CustomerDTO patchRequest = new CustomerDTO();
        patchRequest.setEmail("charlie.updated@example.com");

        mockMvc.perform(patch("/api/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("charlie.updated@example.com")))
                .andExpect(jsonPath("$.displayname", is("Charlie Chaplin")));
    }

    @Test
    void deleteCustomer_removesCustomerAndSubsequentGetReturns404() throws Exception {
        Customer customer = new Customer();
        customer.setDisplayname("Dana Scully");
        customer.setEmail("dana@example.com");
        customer.setPhone("555-9999");
        customer.setBirthday(LocalDate.of(1975, 2, 23));
        customer = customerRepository.save(customer);

        mockMvc.perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", customer.getId()))
                .andExpect(status().isNotFound());
    }
}

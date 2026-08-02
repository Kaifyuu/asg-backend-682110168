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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import th.camt.dto.ProductDTO;
import th.camt.repository.ProductRepository;
import th.mfu.domain.Product;

/** Integration tests for {@link ProductController}. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createProduct_persistsAndReturnsCreatedProduct() throws Exception {
        ProductDTO request = new ProductDTO();
        request.setName("Mechanical Keyboard");
        request.setPrice(129.99);
        request.setDescription("Clicky and satisfying");
        request.setManufactureDate(LocalDate.of(2024, 5, 1));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name", is("Mechanical Keyboard")))
                .andExpect(jsonPath("$.price", is(129.99)));
    }

    @Test
    void listProducts_returnsAllPersistedProducts() throws Exception {
        Product product = new Product();
        product.setName("USB Hub");
        product.setPrice(19.99);
        product.setDescription("4 ports");
        product.setManufactureDate(LocalDate.of(2024, 1, 1));
        productRepository.save(product);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("USB Hub")));
    }

    @Test
    void patchProduct_updatesOnlyProvidedFields() throws Exception {
        Product product = new Product();
        product.setName("Desk Lamp");
        product.setPrice(45.99);
        product.setDescription("LED");
        product.setManufactureDate(LocalDate.of(2024, 2, 1));
        product = productRepository.save(product);

        ProductDTO patchRequest = new ProductDTO();
        patchRequest.setPrice(39.99);

        mockMvc.perform(patch("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(39.99)))
                .andExpect(jsonPath("$.name", is("Desk Lamp")));
    }

    @Test
    void deleteProduct_removesProductAndSubsequentGetReturns404() throws Exception {
        Product product = new Product();
        product.setName("Webcam");
        product.setPrice(59.99);
        product.setDescription("1080p");
        product.setManufactureDate(LocalDate.of(2024, 3, 1));
        product = productRepository.save(product);

        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isNotFound());
    }
}

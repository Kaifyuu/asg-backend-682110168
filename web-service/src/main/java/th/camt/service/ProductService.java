package th.camt.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import th.camt.dto.ProductDTO;
import th.camt.repository.ProductRepository;
import th.mfu.domain.Product;

// Simplest of the three services (Product has no relationships of its own to
// manage) - useful as the baseline example of the create/list/patch/delete +
// entity<->DTO mapping pattern repeated across CustomerService and OrderService.
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTO> list() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO get(Long id) {
        return toDTO(findOrThrow(id));
    }

    public ProductDTO create(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setManufactureDate(dto.getManufactureDate());
        return toDTO(productRepository.save(product));
    }

    public ProductDTO patch(Long id, ProductDTO dto) {
        Product product = findOrThrow(id);
        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getManufactureDate() != null) {
            product.setManufactureDate(dto.getManufactureDate());
        }
        return toDTO(productRepository.save(product));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product " + id + " not found");
        }
        productRepository.deleteById(id);
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product " + id + " not found"));
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setManufactureDate(product.getManufactureDate());
        return dto;
    }
}

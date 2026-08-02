package th.camt.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.camt.dto.CustomerDTO;
import th.camt.service.CustomerService;

// Thin HTTP layer: only handles request/response mapping and status codes.
// All logic (entity<->DTO mapping, partial-update rules, 404s) lives in
// CustomerService. The same Create/List/Update(Patch)/Delete shape is repeated
// in ProductController and OrderController.
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> create(@RequestBody CustomerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(dto));
    }

    @GetMapping
    public List<CustomerDTO> list() {
        return customerService.list();
    }

    @GetMapping("/{id}")
    public CustomerDTO get(@PathVariable Long id) {
        return customerService.get(id);
    }

    @PatchMapping("/{id}")
    public CustomerDTO patch(@PathVariable Long id, @RequestBody CustomerDTO dto) {
        return customerService.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

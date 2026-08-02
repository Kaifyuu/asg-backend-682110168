package th.camt.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import th.camt.dto.CustomerDTO;
import th.camt.dto.ShippingAddressDTO;
import th.camt.repository.CustomerRepository;
import th.mfu.domain.Customer;
import th.mfu.domain.ShippingAddress;

// Service layer sits between the controller and the repository: it's where
// entity<->DTO mapping and business rules (patch semantics, 404 handling) live,
// so the controller stays a thin HTTP wrapper and the entities never leak
// directly over the REST API.
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerDTO> list() {
        return customerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO get(Long id) {
        return toDTO(findOrThrow(id));
    }

    public CustomerDTO create(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setDisplayname(dto.getDisplayname());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setBirthday(dto.getBirthday());

        // setShippingAddress() (defined on Customer) wires the back-reference
        // (address.setCustomer(this)) too, so cascade=ALL on the Customer side
        // saves the ShippingAddress in the same customerRepository.save() call -
        // no separate ShippingAddressRepository.save() needed here.
        if (dto.getShippingAddress() != null) {
            customer.setShippingAddress(toEntity(dto.getShippingAddress()));
        }

        return toDTO(customerRepository.save(customer));
    }

    // PATCH semantics: a field is only overwritten if the client actually sent
    // it (non-null in the parsed DTO). Any field left out of the JSON body stays
    // untouched - this is what distinguishes PATCH from PUT (full replace).
    public CustomerDTO patch(Long id, CustomerDTO dto) {
        Customer customer = findOrThrow(id);

        if (dto.getDisplayname() != null) {
            customer.setDisplayname(dto.getDisplayname());
        }
        if (dto.getEmail() != null) {
            customer.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            customer.setPhone(dto.getPhone());
        }
        if (dto.getBirthday() != null) {
            customer.setBirthday(dto.getBirthday());
        }
        if (dto.getShippingAddress() != null) {
            ShippingAddressDTO addrDto = dto.getShippingAddress();
            ShippingAddress address = customer.getShippingAddress();
            if (address == null) {
                customer.setShippingAddress(toEntity(addrDto));
            } else {
                if (addrDto.getAddress() != null) {
                    address.setAddress(addrDto.getAddress());
                }
                if (addrDto.getCity() != null) {
                    address.setCity(addrDto.getCity());
                }
                if (addrDto.getPostalCode() != null) {
                    address.setPostalCode(addrDto.getPostalCode());
                }
                if (addrDto.getCountry() != null) {
                    address.setCountry(addrDto.getCountry());
                }
            }
        }

        return toDTO(customerRepository.save(customer));
    }

    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer " + id + " not found");
        }
        customerRepository.deleteById(id);
    }

    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer " + id + " not found"));
    }

    private ShippingAddress toEntity(ShippingAddressDTO dto) {
        ShippingAddress address = new ShippingAddress();
        address.setAddress(dto.getAddress());
        address.setCity(dto.getCity());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        return address;
    }

    private CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setDisplayname(customer.getDisplayname());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setBirthday(customer.getBirthday());

        ShippingAddress address = customer.getShippingAddress();
        if (address != null) {
            dto.setShippingAddress(new ShippingAddressDTO(
                    address.getId(), address.getAddress(), address.getCity(),
                    address.getPostalCode(), address.getCountry()));
        }
        return dto;
    }
}

package th.camt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}

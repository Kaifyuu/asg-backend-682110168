package th.camt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

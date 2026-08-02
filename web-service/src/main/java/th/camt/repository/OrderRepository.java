package th.camt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}

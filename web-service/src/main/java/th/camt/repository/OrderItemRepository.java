package th.camt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}

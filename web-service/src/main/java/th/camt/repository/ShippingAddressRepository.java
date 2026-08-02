package th.camt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.domain.ShippingAddress;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
}

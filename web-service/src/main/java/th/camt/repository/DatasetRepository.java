package th.camt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.domain.Dataset;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {
}

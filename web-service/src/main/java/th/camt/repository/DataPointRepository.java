package th.camt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.domain.DataPoint;

public interface DataPointRepository extends JpaRepository<DataPoint, Long> {

    List<DataPoint> findByDatasetId(Long datasetId);
}

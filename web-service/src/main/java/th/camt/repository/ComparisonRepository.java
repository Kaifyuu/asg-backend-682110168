package th.camt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.domain.Comparison;

public interface ComparisonRepository extends JpaRepository<Comparison, Long> {

    void deleteByPointA_Dataset_IdOrPointB_Dataset_Id(Long datasetIdA, Long datasetIdB);
}

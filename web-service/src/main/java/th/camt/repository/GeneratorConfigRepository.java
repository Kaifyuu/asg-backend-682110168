package th.camt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.domain.GeneratorConfig;

public interface GeneratorConfigRepository extends JpaRepository<GeneratorConfig, Long> {
}

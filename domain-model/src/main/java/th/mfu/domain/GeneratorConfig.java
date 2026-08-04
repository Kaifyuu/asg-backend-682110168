package th.mfu.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The settings used to randomly generate a {@link Dataset}'s data points:
 * the inclusive/exclusive bounds, how many points to generate, and an
 * optional seed for reproducible randomness.
 *
 * Relationship: One-to-One with {@link Dataset} (owning side, holds the FK).
 */
@Entity
@Table(name = "generator_config")
public class GeneratorConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "sample_count")
    private Integer sampleCount;

    // Optional: when set, generation is reproducible (same seed -> same points).
    // When null, the service seeds from System.nanoTime() instead.
    @Column(name = "seed")
    private Long seed;

    // Owning side of the One-to-One relationship: this class holds the foreign
    // key column (dataset_id). unique=true is what actually enforces "one
    // config per dataset" at the database level.
    @OneToOne
    @JoinColumn(name = "dataset_id", unique = true)
    @JsonIgnore
    private Dataset dataset;

    public GeneratorConfig() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
}

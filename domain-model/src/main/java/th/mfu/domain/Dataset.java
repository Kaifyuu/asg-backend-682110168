package th.mfu.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A named collection of randomly generated data points.
 *
 * Relationships:
 *  - One-to-One with {@link GeneratorConfig} (the settings used to randomly generate this dataset's points)
 *  - One-to-Many with {@link DataPoint} (a dataset contains many generated points)
 */
@Entity
@Table(name = "dataset")
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Inverse (non-owning) side of the One-to-One relationship: GeneratorConfig
    // owns the foreign key (dataset_id), so this side just points to it via
    // mappedBy. cascade=ALL + orphanRemoval means saving/deleting a Dataset
    // automatically saves/deletes its config too.
    @OneToOne(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private GeneratorConfig generatorConfig;

    // Inverse side of the One-to-Many relationship: DataPoint owns the FK
    // (dataset_id). cascade=ALL + orphanRemoval means addDataPoint()/removing a
    // Dataset also saves/deletes all of its DataPoints.
    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DataPoint> dataPoints = new ArrayList<>();

    public Dataset() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public GeneratorConfig getGeneratorConfig() {
        return generatorConfig;
    }

    /** Keeps both sides of the one-to-one association in sync. */
    public void setGeneratorConfig(GeneratorConfig generatorConfig) {
        if (generatorConfig == null) {
            if (this.generatorConfig != null) {
                this.generatorConfig.setDataset(null);
            }
        } else {
            generatorConfig.setDataset(this);
        }
        this.generatorConfig = generatorConfig;
    }

    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public void addDataPoint(DataPoint dataPoint) {
        dataPoints.add(dataPoint);
        dataPoint.setDataset(this);
    }

    public void clearDataPoints() {
        dataPoints.clear();
    }
}

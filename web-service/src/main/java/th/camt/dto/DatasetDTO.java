package th.camt.dto;

import java.time.LocalDateTime;

public class DatasetDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private GeneratorConfigDTO generatorConfig;
    private Integer dataPointCount;

    public DatasetDTO() {
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

    public GeneratorConfigDTO getGeneratorConfig() {
        return generatorConfig;
    }

    public void setGeneratorConfig(GeneratorConfigDTO generatorConfig) {
        this.generatorConfig = generatorConfig;
    }

    public Integer getDataPointCount() {
        return dataPointCount;
    }

    public void setDataPointCount(Integer dataPointCount) {
        this.dataPointCount = dataPointCount;
    }
}

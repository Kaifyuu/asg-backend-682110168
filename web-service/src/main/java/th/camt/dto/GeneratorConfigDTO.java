package th.camt.dto;

public class GeneratorConfigDTO {

    private Long id;
    private Double minValue;
    private Double maxValue;
    private Integer sampleCount;
    private Long seed;

    public GeneratorConfigDTO() {
    }

    public GeneratorConfigDTO(Long id, Double minValue, Double maxValue, Integer sampleCount, Long seed) {
        this.id = id;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.sampleCount = sampleCount;
        this.seed = seed;
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
}

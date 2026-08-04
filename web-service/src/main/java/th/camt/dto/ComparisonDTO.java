package th.camt.dto;

import java.time.LocalDateTime;

public class ComparisonDTO {

    private Long id;
    private Long pointAId;
    private Long pointBId;
    private Double valueA;
    private Double valueB;
    private String result;
    private Double difference;
    private LocalDateTime comparedAt;

    public ComparisonDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPointAId() {
        return pointAId;
    }

    public void setPointAId(Long pointAId) {
        this.pointAId = pointAId;
    }

    public Long getPointBId() {
        return pointBId;
    }

    public void setPointBId(Long pointBId) {
        this.pointBId = pointBId;
    }

    public Double getValueA() {
        return valueA;
    }

    public void setValueA(Double valueA) {
        this.valueA = valueA;
    }

    public Double getValueB() {
        return valueB;
    }

    public void setValueB(Double valueB) {
        this.valueB = valueB;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Double getDifference() {
        return difference;
    }

    public void setDifference(Double difference) {
        this.difference = difference;
    }

    public LocalDateTime getComparedAt() {
        return comparedAt;
    }

    public void setComparedAt(LocalDateTime comparedAt) {
        this.comparedAt = comparedAt;
    }
}

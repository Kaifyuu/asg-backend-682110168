package th.mfu.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The recorded result of comparing two {@link DataPoint}s against each other.
 *
 * Relationships:
 *  - Many-to-One with {@link DataPoint} (pointA) - many comparisons can reference the same point
 *  - Many-to-One with {@link DataPoint} (pointB) - same as above, second operand
 */
@Entity
@Table(name = "comparison")
public class Comparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side of Many-to-One with DataPoint (first operand).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_a_id")
    private DataPoint pointA;

    // Owning side of Many-to-One with DataPoint (second operand).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_b_id")
    private DataPoint pointB;

    // One of "A_GREATER", "B_GREATER", "EQUAL" - computed by the service when
    // the comparison is created (never trusted from client input).
    @Column(name = "result")
    private String result;

    // abs(pointA.value - pointB.value), snapshotted at comparison time.
    @Column(name = "difference")
    private Double difference;

    @Column(name = "compared_at")
    private LocalDateTime comparedAt;

    public Comparison() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DataPoint getPointA() {
        return pointA;
    }

    public void setPointA(DataPoint pointA) {
        this.pointA = pointA;
    }

    public DataPoint getPointB() {
        return pointB;
    }

    public void setPointB(DataPoint pointB) {
        this.pointB = pointB;
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

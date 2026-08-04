package th.camt.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import th.camt.dto.ComparisonDTO;
import th.camt.repository.ComparisonRepository;
import th.camt.repository.DataPointRepository;
import th.mfu.domain.Comparison;
import th.mfu.domain.DataPoint;

// The "comparing" half of the assignment brief: given two existing DataPoint
// ids (each a Many-to-One reference), work out which is bigger and by how
// much, and persist that as an immutable-ish record.
@Service
public class ComparisonService {

    private static final String A_GREATER = "A_GREATER";
    private static final String B_GREATER = "B_GREATER";
    private static final String EQUAL = "EQUAL";

    private final ComparisonRepository comparisonRepository;
    private final DataPointRepository dataPointRepository;

    public ComparisonService(ComparisonRepository comparisonRepository, DataPointRepository dataPointRepository) {
        this.comparisonRepository = comparisonRepository;
        this.dataPointRepository = dataPointRepository;
    }

    @Transactional(readOnly = true)
    public List<ComparisonDTO> list() {
        return comparisonRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ComparisonDTO get(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public ComparisonDTO create(ComparisonDTO dto) {
        if (dto.getPointAId() == null || dto.getPointBId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pointAId and pointBId are required");
        }
        DataPoint pointA = dataPointRepository.findById(dto.getPointAId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "DataPoint " + dto.getPointAId() + " not found"));
        DataPoint pointB = dataPointRepository.findById(dto.getPointBId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "DataPoint " + dto.getPointBId() + " not found"));

        Comparison comparison = new Comparison();
        comparison.setPointA(pointA);
        comparison.setPointB(pointB);
        comparison.setDifference(Math.abs(pointA.getValue() - pointB.getValue()));
        comparison.setResult(computeResult(pointA.getValue(), pointB.getValue()));
        comparison.setComparedAt(LocalDateTime.now());

        return toDTO(comparisonRepository.save(comparison));
    }

    private String computeResult(double a, double b) {
        if (a > b) {
            return A_GREATER;
        } else if (b > a) {
            return B_GREATER;
        }
        return EQUAL;
    }

    /** Partial update: re-point one side of the comparison and recompute the result. */
    @Transactional
    public ComparisonDTO patch(Long id, ComparisonDTO dto) {
        Comparison comparison = findOrThrow(id);
        if (dto.getPointAId() != null) {
            DataPoint pointA = dataPointRepository.findById(dto.getPointAId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "DataPoint " + dto.getPointAId() + " not found"));
            comparison.setPointA(pointA);
        }
        if (dto.getPointBId() != null) {
            DataPoint pointB = dataPointRepository.findById(dto.getPointBId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "DataPoint " + dto.getPointBId() + " not found"));
            comparison.setPointB(pointB);
        }
        comparison.setDifference(Math.abs(comparison.getPointA().getValue() - comparison.getPointB().getValue()));
        comparison.setResult(computeResult(comparison.getPointA().getValue(), comparison.getPointB().getValue()));
        return toDTO(comparisonRepository.save(comparison));
    }

    @Transactional
    public void delete(Long id) {
        if (!comparisonRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison " + id + " not found");
        }
        comparisonRepository.deleteById(id);
    }

    private Comparison findOrThrow(Long id) {
        return comparisonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison " + id + " not found"));
    }

    private ComparisonDTO toDTO(Comparison comparison) {
        ComparisonDTO dto = new ComparisonDTO();
        dto.setId(comparison.getId());
        dto.setPointAId(comparison.getPointA().getId());
        dto.setPointBId(comparison.getPointB().getId());
        dto.setValueA(comparison.getPointA().getValue());
        dto.setValueB(comparison.getPointB().getValue());
        dto.setResult(comparison.getResult());
        dto.setDifference(comparison.getDifference());
        dto.setComparedAt(comparison.getComparedAt());
        return dto;
    }
}

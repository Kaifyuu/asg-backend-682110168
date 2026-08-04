package th.camt.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import th.camt.dto.DataPointDTO;
import th.camt.repository.DataPointRepository;
import th.camt.repository.DatasetRepository;
import th.mfu.domain.DataPoint;
import th.mfu.domain.Dataset;

// Lets a client add/edit/remove an individual data point directly, on top of
// the bulk random generation in DatasetService - e.g. to manually input a
// value into an existing dataset before running comparisons against it.
@Service
public class DataPointService {

    private final DataPointRepository dataPointRepository;
    private final DatasetRepository datasetRepository;

    public DataPointService(DataPointRepository dataPointRepository, DatasetRepository datasetRepository) {
        this.dataPointRepository = dataPointRepository;
        this.datasetRepository = datasetRepository;
    }

    @Transactional(readOnly = true)
    public List<DataPointDTO> list() {
        return dataPointRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DataPointDTO get(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public DataPointDTO create(DataPointDTO dto) {
        if (dto.getDatasetId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "datasetId is required");
        }
        if (dto.getValue() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value is required");
        }
        Dataset dataset = datasetRepository.findById(dto.getDatasetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Dataset " + dto.getDatasetId() + " not found"));

        DataPoint point = new DataPoint();
        point.setValue(dto.getValue());
        point.setPosition(dto.getPosition() != null ? dto.getPosition() : dataset.getDataPoints().size());
        point.setGeneratedAt(LocalDateTime.now());
        dataset.addDataPoint(point);

        return toDTO(dataPointRepository.save(point));
    }

    @Transactional
    public DataPointDTO patch(Long id, DataPointDTO dto) {
        DataPoint point = findOrThrow(id);
        if (dto.getValue() != null) {
            point.setValue(dto.getValue());
        }
        if (dto.getPosition() != null) {
            point.setPosition(dto.getPosition());
        }
        return toDTO(dataPointRepository.save(point));
    }

    @Transactional
    public void delete(Long id) {
        if (!dataPointRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DataPoint " + id + " not found");
        }
        dataPointRepository.deleteById(id);
    }

    private DataPoint findOrThrow(Long id) {
        return dataPointRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DataPoint " + id + " not found"));
    }

    private DataPointDTO toDTO(DataPoint point) {
        DataPointDTO dto = new DataPointDTO();
        dto.setId(point.getId());
        dto.setDatasetId(point.getDataset() != null ? point.getDataset().getId() : null);
        dto.setValue(point.getValue());
        dto.setPosition(point.getPosition());
        dto.setGeneratedAt(point.getGeneratedAt());
        return dto;
    }
}

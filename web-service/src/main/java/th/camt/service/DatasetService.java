package th.camt.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import th.camt.dto.DatasetDTO;
import th.camt.dto.GeneratorConfigDTO;
import th.camt.repository.ComparisonRepository;
import th.camt.repository.DatasetRepository;
import th.mfu.domain.DataPoint;
import th.mfu.domain.Dataset;
import th.mfu.domain.GeneratorConfig;

// This is where "randomly generated data points" actually happens: create()
// builds a Dataset + its GeneratorConfig (One-to-One), then immediately
// fills the dataset with GeneratorConfig.sampleCount random values in
// [minValue, maxValue) (One-to-Many), all saved through one
// datasetRepository.save() call thanks to cascade=ALL on both associations.
@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final ComparisonRepository comparisonRepository;

    public DatasetService(DatasetRepository datasetRepository, ComparisonRepository comparisonRepository) {
        this.datasetRepository = datasetRepository;
        this.comparisonRepository = comparisonRepository;
    }

    @Transactional(readOnly = true)
    public List<DatasetDTO> list() {
        return datasetRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DatasetDTO get(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public DatasetDTO create(DatasetDTO dto) {
        Dataset dataset = new Dataset();
        dataset.setName(dto.getName());
        dataset.setDescription(dto.getDescription());
        dataset.setCreatedAt(LocalDateTime.now());

        GeneratorConfigDTO configDto = dto.getGeneratorConfig();
        if (configDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "generatorConfig is required");
        }
        if (configDto.getMinValue() == null || configDto.getMaxValue() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "generatorConfig.minValue and maxValue are required");
        }
        if (configDto.getMinValue() >= configDto.getMaxValue()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "generatorConfig.minValue must be less than maxValue");
        }

        GeneratorConfig config = new GeneratorConfig();
        config.setMinValue(configDto.getMinValue());
        config.setMaxValue(configDto.getMaxValue());
        config.setSampleCount(configDto.getSampleCount() != null ? configDto.getSampleCount() : 10);
        config.setSeed(configDto.getSeed());
        dataset.setGeneratorConfig(config);

        generatePoints(dataset, config);

        return toDTO(datasetRepository.save(dataset));
    }

    /** Wipes and re-rolls this dataset's data points using its existing generator config. */
    @Transactional
    public DatasetDTO regenerate(Long id) {
        Dataset dataset = findOrThrow(id);
        GeneratorConfig config = dataset.getGeneratorConfig();
        if (config == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dataset " + id + " has no generatorConfig");
        }
        // Comparisons hold FK references to this dataset's points; regenerating
        // deletes those points, so any comparisons referencing them must go first
        // or the delete fails with a FK constraint violation.
        comparisonRepository.deleteByPointA_Dataset_IdOrPointB_Dataset_Id(id, id);
        dataset.clearDataPoints();
        generatePoints(dataset, config);
        return toDTO(datasetRepository.save(dataset));
    }

    private void generatePoints(Dataset dataset, GeneratorConfig config) {
        Random random = config.getSeed() != null ? new Random(config.getSeed()) : new Random();
        double min = config.getMinValue();
        double max = config.getMaxValue();
        int count = config.getSampleCount() != null ? config.getSampleCount() : 10;

        for (int i = 0; i < count; i++) {
            DataPoint point = new DataPoint();
            point.setValue(min + random.nextDouble() * (max - min));
            point.setPosition(i);
            point.setGeneratedAt(LocalDateTime.now());
            dataset.addDataPoint(point);
        }
    }

    /** Partial update: name/description/generatorConfig settings only - does not touch existing points. */
    @Transactional
    public DatasetDTO patch(Long id, DatasetDTO dto) {
        Dataset dataset = findOrThrow(id);
        if (dto.getName() != null) {
            dataset.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            dataset.setDescription(dto.getDescription());
        }
        if (dto.getGeneratorConfig() != null) {
            GeneratorConfigDTO configDto = dto.getGeneratorConfig();
            GeneratorConfig config = dataset.getGeneratorConfig();
            if (config != null) {
                if (configDto.getMinValue() != null) {
                    config.setMinValue(configDto.getMinValue());
                }
                if (configDto.getMaxValue() != null) {
                    config.setMaxValue(configDto.getMaxValue());
                }
                if (configDto.getSampleCount() != null) {
                    config.setSampleCount(configDto.getSampleCount());
                }
                if (configDto.getSeed() != null) {
                    config.setSeed(configDto.getSeed());
                }
            }
        }
        return toDTO(datasetRepository.save(dataset));
    }

    @Transactional
    public void delete(Long id) {
        if (!datasetRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset " + id + " not found");
        }
        datasetRepository.deleteById(id);
    }

    private Dataset findOrThrow(Long id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset " + id + " not found"));
    }

    private DatasetDTO toDTO(Dataset dataset) {
        DatasetDTO dto = new DatasetDTO();
        dto.setId(dataset.getId());
        dto.setName(dataset.getName());
        dto.setDescription(dataset.getDescription());
        dto.setCreatedAt(dataset.getCreatedAt());
        dto.setDataPointCount(dataset.getDataPoints().size());

        GeneratorConfig config = dataset.getGeneratorConfig();
        if (config != null) {
            dto.setGeneratorConfig(new GeneratorConfigDTO(
                    config.getId(), config.getMinValue(), config.getMaxValue(),
                    config.getSampleCount(), config.getSeed()));
        }
        return dto;
    }
}

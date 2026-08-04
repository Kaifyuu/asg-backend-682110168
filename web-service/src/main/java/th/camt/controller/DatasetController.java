package th.camt.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.camt.dto.DatasetDTO;
import th.camt.service.DatasetService;

// Thin HTTP layer: only handles request/response mapping and status codes.
// All logic (entity<->DTO mapping, random generation, partial-update rules,
// 404s) lives in DatasetService. The same Create/List/Update(Patch)/Delete
// shape is repeated in DataPointController and ComparisonController.
@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    // Create: creates a Dataset + GeneratorConfig and immediately fills it with
    // sampleCount randomly generated DataPoints in [minValue, maxValue).
    @PostMapping
    public ResponseEntity<DatasetDTO> create(@RequestBody DatasetDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(datasetService.create(dto));
    }

    @GetMapping
    public List<DatasetDTO> list() {
        return datasetService.list();
    }

    @GetMapping("/{id}")
    public DatasetDTO get(@PathVariable Long id) {
        return datasetService.get(id);
    }

    @PatchMapping("/{id}")
    public DatasetDTO patch(@PathVariable Long id, @RequestBody DatasetDTO dto) {
        return datasetService.patch(id, dto);
    }

    // Re-rolls a fresh random sample for an existing dataset, reusing its config.
    @PostMapping("/{id}/regenerate")
    public DatasetDTO regenerate(@PathVariable Long id) {
        return datasetService.regenerate(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        datasetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

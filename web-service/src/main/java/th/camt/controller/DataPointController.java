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

import th.camt.dto.DataPointDTO;
import th.camt.service.DataPointService;

@RestController
@RequestMapping("/api/data-points")
public class DataPointController {

    private final DataPointService dataPointService;

    public DataPointController(DataPointService dataPointService) {
        this.dataPointService = dataPointService;
    }

    // Create: manually input a single data point into an existing dataset.
    @PostMapping
    public ResponseEntity<DataPointDTO> create(@RequestBody DataPointDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dataPointService.create(dto));
    }

    @GetMapping
    public List<DataPointDTO> list() {
        return dataPointService.list();
    }

    @GetMapping("/{id}")
    public DataPointDTO get(@PathVariable Long id) {
        return dataPointService.get(id);
    }

    @PatchMapping("/{id}")
    public DataPointDTO patch(@PathVariable Long id, @RequestBody DataPointDTO dto) {
        return dataPointService.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dataPointService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

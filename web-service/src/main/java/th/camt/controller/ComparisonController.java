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

import th.camt.dto.ComparisonDTO;
import th.camt.service.ComparisonService;

@RestController
@RequestMapping("/api/comparisons")
public class ComparisonController {

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    // Create: compares two existing DataPoints by id and stores which is
    // greater (or equal) along with the absolute difference.
    @PostMapping
    public ResponseEntity<ComparisonDTO> create(@RequestBody ComparisonDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comparisonService.create(dto));
    }

    @GetMapping
    public List<ComparisonDTO> list() {
        return comparisonService.list();
    }

    @GetMapping("/{id}")
    public ComparisonDTO get(@PathVariable Long id) {
        return comparisonService.get(id);
    }

    @PatchMapping("/{id}")
    public ComparisonDTO patch(@PathVariable Long id, @RequestBody ComparisonDTO dto) {
        return comparisonService.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        comparisonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

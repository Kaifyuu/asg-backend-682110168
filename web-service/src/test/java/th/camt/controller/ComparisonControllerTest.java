package th.camt.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import th.camt.dto.ComparisonDTO;
import th.camt.repository.DataPointRepository;
import th.camt.repository.DatasetRepository;
import th.mfu.domain.DataPoint;
import th.mfu.domain.Dataset;

/**
 * Integration tests for {@link ComparisonController}. These exercise the two
 * Many-to-One relationships from Comparison to DataPoint (pointA and pointB).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ComparisonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DataPointRepository dataPointRepository;

    private Long createDataPoint(double value) {
        Dataset dataset = new Dataset();
        dataset.setName("Comparison Test Dataset");
        dataset = datasetRepository.save(dataset);

        DataPoint point = new DataPoint();
        point.setValue(value);
        dataset.addDataPoint(point);
        return dataPointRepository.save(point).getId();
    }

    @Test
    void createComparison_computesGreaterResultAndDifference() throws Exception {
        Long pointAId = createDataPoint(10.0);
        Long pointBId = createDataPoint(4.0);

        ComparisonDTO request = new ComparisonDTO();
        request.setPointAId(pointAId);
        request.setPointBId(pointBId);

        mockMvc.perform(post("/api/comparisons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.result", is("A_GREATER")))
                .andExpect(jsonPath("$.difference", is(6.0)));
    }

    @Test
    void listComparisons_returnsCreatedComparison() throws Exception {
        Long pointAId = createDataPoint(1.0);
        Long pointBId = createDataPoint(1.0);

        ComparisonDTO request = new ComparisonDTO();
        request.setPointAId(pointAId);
        request.setPointBId(pointBId);

        mockMvc.perform(post("/api/comparisons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/comparisons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].result", is("EQUAL")));
    }

    @Test
    void patchComparison_repointsSecondOperandAndRecomputesResult() throws Exception {
        Long pointAId = createDataPoint(5.0);
        Long pointBId = createDataPoint(50.0);
        Long pointCId = createDataPoint(1.0);

        ComparisonDTO request = new ComparisonDTO();
        request.setPointAId(pointAId);
        request.setPointBId(pointBId);

        String response = mockMvc.perform(post("/api/comparisons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        Long comparisonId = objectMapper.readTree(response).get("id").asLong();

        ComparisonDTO patchRequest = new ComparisonDTO();
        patchRequest.setPointBId(pointCId);

        mockMvc.perform(patch("/api/comparisons/{id}", comparisonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("A_GREATER")))
                .andExpect(jsonPath("$.difference", is(4.0)));
    }

    @Test
    void deleteComparison_removesComparisonAndSubsequentGetReturns404() throws Exception {
        Long pointAId = createDataPoint(2.0);
        Long pointBId = createDataPoint(3.0);

        ComparisonDTO request = new ComparisonDTO();
        request.setPointAId(pointAId);
        request.setPointBId(pointBId);

        String response = mockMvc.perform(post("/api/comparisons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        Long comparisonId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/comparisons/{id}", comparisonId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/comparisons/{id}", comparisonId))
                .andExpect(status().isNotFound());
    }
}

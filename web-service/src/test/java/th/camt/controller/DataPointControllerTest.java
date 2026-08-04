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

import th.camt.dto.DataPointDTO;
import th.camt.repository.DatasetRepository;
import th.mfu.domain.Dataset;

/**
 * Integration tests for {@link DataPointController}. Also exercises the
 * Many-to-One relationship between DataPoint and Dataset.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DataPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DatasetRepository datasetRepository;

    private Long createEmptyDataset() {
        Dataset dataset = new Dataset();
        dataset.setName("Manual Dataset");
        dataset.setDescription("For data point tests");
        return datasetRepository.save(dataset).getId();
    }

    @Test
    void createDataPoint_persistsPointLinkedToDataset() throws Exception {
        Long datasetId = createEmptyDataset();

        DataPointDTO request = new DataPointDTO();
        request.setDatasetId(datasetId);
        request.setValue(42.5);

        mockMvc.perform(post("/api/data-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.datasetId", is(datasetId.intValue())))
                .andExpect(jsonPath("$.value", is(42.5)));
    }

    @Test
    void listDataPoints_returnsCreatedPoint() throws Exception {
        Long datasetId = createEmptyDataset();

        DataPointDTO request = new DataPointDTO();
        request.setDatasetId(datasetId);
        request.setValue(7.0);

        mockMvc.perform(post("/api/data-points")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/data-points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].value", is(7.0)));
    }

    @Test
    void patchDataPoint_updatesValue() throws Exception {
        Long datasetId = createEmptyDataset();

        DataPointDTO request = new DataPointDTO();
        request.setDatasetId(datasetId);
        request.setValue(10.0);

        String response = mockMvc.perform(post("/api/data-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        Long pointId = objectMapper.readTree(response).get("id").asLong();

        DataPointDTO patchRequest = new DataPointDTO();
        patchRequest.setValue(99.0);

        mockMvc.perform(patch("/api/data-points/{id}", pointId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(99.0)));
    }

    @Test
    void deleteDataPoint_removesPointAndSubsequentGetReturns404() throws Exception {
        Long datasetId = createEmptyDataset();

        DataPointDTO request = new DataPointDTO();
        request.setDatasetId(datasetId);
        request.setValue(3.0);

        String response = mockMvc.perform(post("/api/data-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        Long pointId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/data-points/{id}", pointId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/data-points/{id}", pointId))
                .andExpect(status().isNotFound());
    }
}

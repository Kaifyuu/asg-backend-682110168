package th.camt.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import th.camt.dto.DatasetDTO;
import th.camt.dto.GeneratorConfigDTO;
import th.camt.repository.DatasetRepository;

/**
 * Integration tests for {@link DatasetController}.
 *
 * @SpringBootTest boots the real application context (controller, service,
 * repository, H2 database) - not mocks - so this exercises the full stack.
 * @Transactional wraps each test method in a transaction that's rolled back
 * afterwards, so tests don't interfere with each other or depend on the seed
 * data in data.sql (which is disabled for tests).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DatasetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DatasetRepository datasetRepository;

    private DatasetDTO newDatasetRequest(String name, int sampleCount) {
        DatasetDTO request = new DatasetDTO();
        request.setName(name);
        request.setDescription("Randomly generated numbers for testing");

        GeneratorConfigDTO config = new GeneratorConfigDTO();
        config.setMinValue(0.0);
        config.setMaxValue(100.0);
        config.setSampleCount(sampleCount);
        config.setSeed(42L);
        request.setGeneratorConfig(config);
        return request;
    }

    @Test
    void createDataset_generatesRequestedNumberOfRandomDataPoints() throws Exception {
        DatasetDTO request = newDatasetRequest("Test Dataset", 5);

        mockMvc.perform(post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name", is("Test Dataset")))
                .andExpect(jsonPath("$.dataPointCount", is(5)))
                .andExpect(jsonPath("$.generatorConfig.minValue", is(0.0)))
                .andExpect(jsonPath("$.generatorConfig.maxValue", is(100.0)));

        Assertions.assertEquals(1, datasetRepository.count());
    }

    @Test
    void listDatasets_returnsAllPersistedDatasets() throws Exception {
        mockMvc.perform(post("/api/datasets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDatasetRequest("List Dataset", 3))));

        mockMvc.perform(get("/api/datasets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("List Dataset")));
    }

    @Test
    void patchDataset_updatesOnlyProvidedFields() throws Exception {
        String response = mockMvc.perform(post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDatasetRequest("Original Name", 4))))
                .andReturn().getResponse().getContentAsString();
        Long datasetId = objectMapper.readTree(response).get("id").asLong();

        DatasetDTO patchRequest = new DatasetDTO();
        patchRequest.setDescription("Updated description");

        mockMvc.perform(patch("/api/datasets/{id}", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.name", is("Original Name")));
    }

    @Test
    void deleteDataset_removesDatasetAndSubsequentGetReturns404() throws Exception {
        String response = mockMvc.perform(post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDatasetRequest("To Delete", 2))))
                .andReturn().getResponse().getContentAsString();
        Long datasetId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/datasets/{id}", datasetId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/datasets/{id}", datasetId))
                .andExpect(status().isNotFound());
    }
}

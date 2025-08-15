package com.reliaquest.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test_GetAllEmployees_Success() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void test_GetEmployeesByNameSearch_found() throws Exception {
        mockMvc.perform(get("/search/John"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].employee_name",containsString("John")));
    }

    @Test
    void test_GetEmployeesByNameSearch_notFound() throws Exception {
        mockMvc.perform(get("/search/RandomName"))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_GetEmployeeById_Success() throws Exception {
        CreateMockEmployeeInput input = new CreateMockEmployeeInput();
        input.setName("Jackinda Ardren");
        input.setSalary(500000);
        input.setAge(30);
        input.setTitle("Junior developer");
        MvcResult createResult = mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        String responseBody = createResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
        String id = root.get("id").asText();

        mockMvc.perform(get("/" + id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employee_name").value("Jackinda Ardren"));
    }


    @Test
    void test_GetHighestSalaryOfEmployees_Success() throws Exception {
        mockMvc.perform(get("/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void test_GetTopTenHighestEarningEmployeeNames_Success() throws Exception {
        mockMvc.perform(get("/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$",hasSize(10)));
    }

    @Test
    void test_CreateEmployee_Success() throws Exception {
        CreateMockEmployeeInput input = new CreateMockEmployeeInput();
        input.setName("Jackinda Ardren");
        input.setSalary(500000);
        input.setAge(30);
        input.setTitle("Junior developer");
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("Jackinda Ardren"));
    }

    @Test
    void test_DeleteEmployeeById_Success() throws Exception {
        CreateMockEmployeeInput input = new CreateMockEmployeeInput();
        input.setName("Jackinda Ardren");
        input.setSalary(500000);
        input.setAge(30);
        input.setTitle("Junior developer");
        MvcResult createResult = mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        String responseBody = createResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
        String validId = root.get("id").asText();

        mockMvc.perform(delete("/" + validId))
                .andExpect(status().isOk())
                .andExpect(content().string(root.get("employee_name").asText()));
    }

    @Test
    void test_DeleteEmployee_EmployeeNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(delete("/" + randomId))
                .andExpect(content().string("Employee with id "+randomId+" not found!"));
    }


}

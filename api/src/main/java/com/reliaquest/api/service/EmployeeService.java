package com.reliaquest.api.service;

import com.reliaquest.api.web.RateLimitingHandler;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import com.reliaquest.server.model.Response;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmployeeService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mockemployee.service.url}")
    private String mockEmployeeServiceUrl;

    @Cacheable("allEmployees")
    public List<MockEmployee> getAllEmployees() {
        ResponseEntity<Response<List<MockEmployee>>> response = RateLimitingHandler.retryOnRateLimit(() -> restTemplate.exchange(
                mockEmployeeServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Response<List<MockEmployee>>>() {}
        ));
        if (response.getBody() == null) {
            log.error("Received empty response body when fetching employees.");
            throw new IllegalStateException("Empty response body from employee API");
        }
        log.info("Successfully fetched {} employees.", response.getBody().data().size());
        return response.getBody().data();
    }

    @Cacheable(value = "searchEmployees", key = "#employeeName.toLowerCase()")
    public List<MockEmployee> getEmployeesByNameSearch(String employeeName) {
        log.info("Searching for employees by name containing '{}'", employeeName);
        return RateLimitingHandler.retryOnRateLimit(() -> getAllEmployees().stream()
                .filter(mockEmployee -> mockEmployee.getName().toLowerCase().contains(employeeName.toLowerCase()))
                .collect(Collectors.toList()));
    }

    @Cacheable(value = "employeeById", key = "#id")
    public MockEmployee getEmployeeById(UUID id) {
        log.info("Fetching employee by ID: {}", id);
        String urlEndpoint = mockEmployeeServiceUrl + "/" + id;

        ResponseEntity<Response<MockEmployee>> mockEmployeeResponse = null;

        try {
            mockEmployeeResponse = RateLimitingHandler.retryOnRateLimit(() -> restTemplate.exchange(
                    urlEndpoint,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Response<MockEmployee>>() {}
            ));
        } catch (Exception e) {
            log.error("Error occurred while fetching employee: {}", e.getMessage());
            throw new IllegalArgumentException("Employee with id " + id + " not found!");
        }

        assert mockEmployeeResponse != null;
        if (mockEmployeeResponse.getBody() == null || mockEmployeeResponse.getBody().data() == null) {
            log.error("Employee not found with ID: {}", id);
            throw new IllegalArgumentException("Employee with id " + id + " not found!");
        }

        return mockEmployeeResponse.getBody().data();
    }



    @Cacheable("highestSalary")
    public Integer getHighestSalaryAmongstEmployees() {
        int highest = RateLimitingHandler.retryOnRateLimit(() -> getAllEmployees().stream()
                .mapToInt(MockEmployee::getSalary)
                .max()
                .orElse(0));
        log.info("Highest salary found: {}", highest);
        return highest;
    }

    @Cacheable("top10HighestEarning")
    public List<String> getTop10HighestEarningEmployeeNames() {
        log.info("Fetching top 10 highest earning employee names...");
        List<String> top10Names = RateLimitingHandler.retryOnRateLimit(() -> getAllEmployees().stream()
                .sorted(Comparator.comparing(MockEmployee::getSalary).reversed())
                .limit(10)
                .map(MockEmployee::getName)
                .collect(Collectors.toList()));
        log.info("Top 10 highest earners: {}", top10Names);
        return top10Names;
    }

    @CacheEvict(value = {"allEmployees", "searchEmployees", "employeeById", "highestSalary", "top10HighestEarning"},
            allEntries = true)
    public MockEmployee createEmployee(CreateMockEmployeeInput input) {
        log.info("Creating new employee: {}", input.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateMockEmployeeInput> requestEntity = new HttpEntity<>(input, headers);

        ResponseEntity<Response<MockEmployee>> response = RateLimitingHandler.retryOnRateLimit(() -> restTemplate.exchange(
                mockEmployeeServiceUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Response<MockEmployee>>() {}
        ));

        if (response.getBody() == null) {
            log.error("Failed to create employee: response body was null.");
            throw new IllegalStateException("Exception occurred while creating new employee details!");
        }

        log.info("Successfully created employee: {}", response.getBody().data().getName());
        return response.getBody().data();
    }

    @CacheEvict(value = {"allEmployees", "searchEmployees", "employeeById", "highestSalary", "top10HighestEarning"},
            allEntries = true)
    public String deleteEmployeeById(UUID id) {
        log.info("Deleting employee by ID: {}", id);

        MockEmployee mockEmployee;
        try {
            mockEmployee = getEmployeeById(id);
        }catch (Exception e){
            log.error(e.getMessage());
            return e.getMessage();
        }
        DeleteMockEmployeeInput deleteMockEmployeeInput = new DeleteMockEmployeeInput();
        deleteMockEmployeeInput.setName(mockEmployee.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeleteMockEmployeeInput> requestEntity = new HttpEntity<>(deleteMockEmployeeInput, headers);

        ResponseEntity<Response<Boolean>> response = RateLimitingHandler.retryOnRateLimit(() -> restTemplate.exchange(
                mockEmployeeServiceUrl,
                HttpMethod.DELETE,
                requestEntity,
                new ParameterizedTypeReference<Response<Boolean>>() {}
        ));

        Boolean isDeleted = Objects.requireNonNull(response.getBody()).data();
        if (Boolean.FALSE.equals(isDeleted)) {
            log.warn("Employee to be deleted not found with ID: {}", id);
            throw new IllegalArgumentException("Employee to be deleted not found!");
        }

        log.info("Successfully deleted employee: {}", mockEmployee.getName());
        return mockEmployee.getName();
    }

}

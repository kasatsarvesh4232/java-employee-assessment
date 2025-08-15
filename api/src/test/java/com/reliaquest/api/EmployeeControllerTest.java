package com.reliaquest.api;


import com.reliaquest.api.controller.impl.EmployeeController;
import com.reliaquest.api.service.EmployeeService;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @MockBean
    EmployeeService employeeService;

    @Autowired
    private MockMvc mockMvc;

    private MockEmployee mockEmployee1 = new MockEmployee(UUID.randomUUID(),"John Grame",800000,27,"Senior developer","johngrame@gmail.com");
    private MockEmployee mockEmployee2 = new MockEmployee(UUID.randomUUID(),"Steve Smith",700000,26,"Junior developer","stevesmith@gmail.com");
    private MockEmployee mockEmployee3 = new MockEmployee(UUID.randomUUID(),"John Adams",650000,28,"Mid developer","johnadamas@gmail.com");
    private List<MockEmployee> mockEmployeesList = new ArrayList<>();

    @BeforeEach
    public void setUp(){
        mockEmployeesList.add(mockEmployee1);
        mockEmployeesList.add(mockEmployee2);
        mockEmployeesList.add(mockEmployee3);
    }

    @Test
    public void test_getAllEmployees_Success() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(mockEmployeesList);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("John Grame"))
                .andExpect(jsonPath("$[0].id").value(mockEmployee1.getId().toString()));
    }

    @Test     //Case when employee list is empty
    public void test_getAllEmployees_Fails() throws Exception{
        when(employeeService.getAllEmployees()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void test_getEmployeesByNameSearch_Success() throws Exception {
        when(employeeService.getEmployeesByNameSearch(eq("John"))).thenReturn(List.of(mockEmployee1,mockEmployee3));

        mockMvc.perform(get("/search/John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void test_GetHighestSalaryOfEmployees_Success() throws Exception {
        when(employeeService.getHighestSalaryAmongstEmployees()).thenReturn(900000);

        mockMvc.perform(get("/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("900000"));
    }

    @Test
    public void test_getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        List<String> names = List.of("Willy march","Daisy Donor");
        when(employeeService.getTop10HighestEarningEmployeeNames()).thenReturn(names);

        mockMvc.perform(get("/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0]",is("Willy march")));
    }

    @Test
    void test_createEmployee_Success() throws Exception {
        CreateMockEmployeeInput mockEmployeeInput = new CreateMockEmployeeInput();
        mockEmployeeInput.setName("Alice");
        mockEmployeeInput.setAge(29);
        mockEmployeeInput.setSalary(7899000);
        mockEmployeeInput.setTitle("Project Manager");
        MockEmployee mockEmployee = new MockEmployee(UUID.randomUUID(),mockEmployeeInput.getName(),mockEmployeeInput.getSalary(),mockEmployeeInput.getAge(),mockEmployeeInput.getTitle(),"Alice@gmail.com");
        when(employeeService.createEmployee(any(CreateMockEmployeeInput.class))).thenReturn(mockEmployee);

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Alice",
                                    "salary": 600000,
                                    "age": 30,
                                    "title": "Engineer",
                                    "email": "alice@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name", is("Alice")));
    }

    @Test
    void testDeleteEmployeeById() throws Exception {
        UUID id = UUID.randomUUID();
        when(employeeService.deleteEmployeeById(id)).thenReturn("John Grame");

        mockMvc.perform(delete("/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string("John Grame"));
    }
}


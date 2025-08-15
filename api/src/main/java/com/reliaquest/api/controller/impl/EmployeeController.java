package com.reliaquest.api.controller.impl;

import com.reliaquest.api.controller.IEmployeeController;
import com.reliaquest.api.service.EmployeeService;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class EmployeeController implements IEmployeeController<MockEmployee, CreateMockEmployeeInput> {


    @Autowired
    private EmployeeService employeeService;

    @Override
    public ResponseEntity<List<MockEmployee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Override
    public ResponseEntity<List<MockEmployee>> getEmployeesByNameSearch(String searchString) {
        List<MockEmployee> result = employeeService.getEmployeesByNameSearch(searchString);

        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        return ResponseEntity.ok(result);
    }



    @Override
    public ResponseEntity<MockEmployee> getEmployeeById(String id) {
        UUID uuid = UUID.fromString(id);
        MockEmployee employee = employeeService.getEmployeeById(uuid);
        return ResponseEntity.ok(employee);

    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        int highestSalary = employeeService.getHighestSalaryAmongstEmployees();
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> names = employeeService.getTop10HighestEarningEmployeeNames();
        return ResponseEntity.ok(names);
    }

    @Override
    public ResponseEntity<MockEmployee> createEmployee(@RequestBody CreateMockEmployeeInput input) {
        MockEmployee employee = employeeService.createEmployee(input);
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
            UUID uuid;
            try {
               uuid  = UUID.fromString(id);
            }catch (IllegalArgumentException e){
               return ResponseEntity.badRequest().body("Invalid id format!");
            }
            String deletedName = employeeService.deleteEmployeeById(uuid);
            return ResponseEntity.ok(deletedName);
    }


}
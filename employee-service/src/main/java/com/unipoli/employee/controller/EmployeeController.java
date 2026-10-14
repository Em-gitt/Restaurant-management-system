package com.unipoli.employee.controller;

import com.unipoli.employee.dto.CreateEmployeeRequest;
import com.unipoli.employee.dto.EmployeeResponse;
import com.unipoli.employee.dto.UpdateEmployeeRequest;
import com.unipoli.employee.model.EmployeeRole;
import com.unipoli.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> findAll(
            @RequestParam(required = false) EmployeeRole role,
            @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(employeeService.findAll(role, active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }
}

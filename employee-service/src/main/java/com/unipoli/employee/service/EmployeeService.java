package com.unipoli.employee.service;

import com.unipoli.employee.dto.CreateEmployeeRequest;
import com.unipoli.employee.dto.EmployeeResponse;
import com.unipoli.employee.dto.UpdateEmployeeRequest;
import com.unipoli.employee.exception.ResourceNotFoundException;
import com.unipoli.employee.model.Employee;
import com.unipoli.employee.model.EmployeeRole;
import com.unipoli.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public EmployeeResponse create(CreateEmployeeRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Employee with email " + request.email() + " already exists");
        }

        Employee employee = new Employee();
        employee.setUserId(request.userId());
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setEmail(request.email().trim().toLowerCase());
        employee.setPhone(request.phone());
        employee.setRole(request.role());
        employee.setShift(request.shift());
        employee.setActive(true);
        employee.setHireDate(request.hireDate() != null ? request.hireDate() : LocalDate.now());

        Employee saved = repository.save(employee);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll(EmployeeRole role, Boolean active) {
        List<Employee> list;
        if (role != null && active != null) {
            list = repository.findByRoleAndActive(role, active);
        } else if (role != null) {
            list = repository.findByRole(role);
        } else if (active != null) {
            list = repository.findByActive(active);
        } else {
            list = repository.findAll();
        }
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (request.firstName() != null && !request.firstName().isBlank()) {
            employee.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            employee.setLastName(request.lastName().trim());
        }
        if (request.phone() != null) {
            employee.setPhone(request.phone());
        }
        if (request.role() != null) {
            employee.setRole(request.role());
        }
        if (request.shift() != null) {
            employee.setShift(request.shift());
        }
        if (request.active() != null) {
            employee.setActive(request.active());
        }

        Employee updated = repository.save(employee);
        return toResponse(updated);
    }

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getUserId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getRole(),
                employee.getShift(),
                employee.getActive(),
                employee.getHireDate(),
                employee.getCreatedAt()
        );
    }
}

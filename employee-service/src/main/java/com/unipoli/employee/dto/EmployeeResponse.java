package com.unipoli.employee.dto;

import com.unipoli.employee.model.EmployeeRole;
import com.unipoli.employee.model.Shift;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        EmployeeRole role,
        Shift shift,
        Boolean active,
        LocalDate hireDate,
        LocalDateTime createdAt
) {
}

package com.unipoli.employee.dto;

import com.unipoli.employee.model.EmployeeRole;
import com.unipoli.employee.model.Shift;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateEmployeeRequest(
        Long userId,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,

        @NotNull(message = "Role is required")
        EmployeeRole role,

        @NotNull(message = "Shift is required")
        Shift shift,

        LocalDate hireDate
) {
}

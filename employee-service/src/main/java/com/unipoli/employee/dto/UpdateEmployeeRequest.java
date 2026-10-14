package com.unipoli.employee.dto;

import com.unipoli.employee.model.EmployeeRole;
import com.unipoli.employee.model.Shift;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequest(
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,

        EmployeeRole role,

        Shift shift,

        Boolean active
) {
}

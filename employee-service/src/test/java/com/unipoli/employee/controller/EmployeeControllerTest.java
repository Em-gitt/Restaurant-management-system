package com.unipoli.employee.controller;

import com.unipoli.employee.dto.CreateEmployeeRequest;
import com.unipoli.employee.dto.EmployeeResponse;
import com.unipoli.employee.model.EmployeeRole;
import com.unipoli.employee.model.Shift;
import com.unipoli.employee.security.JwtAuthenticationFilter;
import com.unipoli.employee.security.JwtService;
import com.unipoli.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EmployeeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, JwtService.class})
)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void create_WhenValid_ShouldReturnCreated() throws Exception {
        EmployeeResponse response = new EmployeeResponse(
                1L,
                1L,
                "Ana",
                "Lopez",
                "ana.lopez@restaurant.com",
                "555-5678",
                EmployeeRole.CHEF,
                Shift.FULL_TIME,
                true,
                LocalDate.of(2026, 1, 15),
                LocalDateTime.now()
        );

        when(employeeService.create(any(CreateEmployeeRequest.class))).thenReturn(response);

        String json = """
                {
                    "userId": 1,
                    "firstName": "Ana",
                    "lastName": "Lopez",
                    "email": "ana.lopez@restaurant.com",
                    "phone": "555-5678",
                    "role": "CHEF",
                    "shift": "FULL_TIME",
                    "hireDate": "2026-01-15"
                }
                """;

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana.lopez@restaurant.com"))
                .andExpect(jsonPath("$.role").value("CHEF"));
    }

    @Test
    void findAll_ShouldReturnList() throws Exception {
        EmployeeResponse response = new EmployeeResponse(
                1L,
                1L,
                "Ana",
                "Lopez",
                "ana.lopez@restaurant.com",
                "555-5678",
                EmployeeRole.CHEF,
                Shift.FULL_TIME,
                true,
                LocalDate.of(2026, 1, 15),
                LocalDateTime.now()
        );

        when(employeeService.findAll(null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ana"));
    }

    @Test
    void create_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        String json = """
                {
                    "userId": 1,
                    "firstName": "Ana",
                    "lastName": "Lopez",
                    "email": "not-an-email",
                    "phone": "555-5678",
                    "role": "CHEF",
                    "shift": "FULL_TIME",
                    "hireDate": "2026-01-15"
                }
                """;

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}

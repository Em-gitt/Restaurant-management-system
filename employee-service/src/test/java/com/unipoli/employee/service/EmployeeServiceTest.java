package com.unipoli.employee.service;

import com.unipoli.employee.dto.CreateEmployeeRequest;
import com.unipoli.employee.dto.EmployeeResponse;
import com.unipoli.employee.dto.UpdateEmployeeRequest;
import com.unipoli.employee.exception.ResourceNotFoundException;
import com.unipoli.employee.model.Employee;
import com.unipoli.employee.model.EmployeeRole;
import com.unipoli.employee.model.Shift;
import com.unipoli.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setUserId(10L);
        employee.setFirstName("Juan");
        employee.setLastName("Perez");
        employee.setEmail("juan.perez@restaurant.com");
        employee.setPhone("555-1234");
        employee.setRole(EmployeeRole.WAITER);
        employee.setShift(Shift.MORNING);
        employee.setActive(true);
        employee.setHireDate(LocalDate.now());
        employee.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void create_WhenValidRequest_ShouldReturnEmployeeResponse() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                10L,
                "Juan",
                "Perez",
                "juan.perez@restaurant.com",
                "555-1234",
                EmployeeRole.WAITER,
                Shift.MORNING,
                LocalDate.now()
        );

        when(repository.existsByEmail("juan.perez@restaurant.com")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("juan.perez@restaurant.com", response.email());
        assertEquals(EmployeeRole.WAITER, response.role());
        verify(repository, times(1)).save(any(Employee.class));
    }

    @Test
    void create_WhenEmailAlreadyExists_ShouldThrowIllegalArgumentException() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                10L,
                "Juan",
                "Perez",
                "juan.perez@restaurant.com",
                "555-1234",
                EmployeeRole.WAITER,
                Shift.MORNING,
                LocalDate.now()
        );

        when(repository.existsByEmail("juan.perez@restaurant.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).save(any(Employee.class));
    }

    @Test
    void findById_WhenFound_ShouldReturnResponse() {
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponse response = service.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Juan", response.firstName());
    }

    @Test
    void findById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void update_WhenValid_ShouldUpdateAndReturnResponse() {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "Carlos",
                "Perez",
                "555-9999",
                EmployeeRole.CHEF,
                Shift.AFTERNOON,
                true
        );

        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse response = service.update(1L, request);

        assertNotNull(response);
        verify(repository, times(1)).save(employee);
    }

    @Test
    void findAll_WithFilters_ShouldReturnFilteredList() {
        when(repository.findByRoleAndActive(EmployeeRole.WAITER, true)).thenReturn(List.of(employee));

        List<EmployeeResponse> list = service.findAll(EmployeeRole.WAITER, true);

        assertEquals(1, list.size());
        assertEquals(EmployeeRole.WAITER, list.get(0).role());
    }
}

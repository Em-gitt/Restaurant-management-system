package com.unipoli.employee.repository;

import com.unipoli.employee.model.Employee;
import com.unipoli.employee.model.EmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Employee> findByUserId(Long userId);

    List<Employee> findByRole(EmployeeRole role);

    List<Employee> findByActive(Boolean active);

    List<Employee> findByRoleAndActive(EmployeeRole role, Boolean active);
}

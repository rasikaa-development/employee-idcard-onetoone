package com.example.employee.repository;

import com.example.employee.entity.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // @EntityGraph loads the ID card in the SAME SQL query (join)
    // instead of one extra query per employee.
    @Override
    @EntityGraph(attributePaths = "idCard")
    List<Employee> findAll();

    @Override
    @EntityGraph(attributePaths = "idCard")
    Optional<Employee> findById(Long employeeId);
}

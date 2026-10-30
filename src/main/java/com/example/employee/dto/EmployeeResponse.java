package com.example.employee.dto;

import com.example.employee.entity.Employee;

public record EmployeeResponse(Long employeeId, String employeeName, String department, IDCardResponse idCard) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                employee.getDepartment(),
                IDCardResponse.from(employee.getIdCard()));
    }
}

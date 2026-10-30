package com.example.employee.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body of POST / PUT /employees - an employee is always sent together with its ID card. */
public record EmployeeRequest(
        @NotBlank @Size(max = 100) String employeeName,
        @NotBlank @Size(max = 50) String department,
        @NotNull(message = "every employee must have exactly one ID card") @Valid IDCardRequest idCard) {
}

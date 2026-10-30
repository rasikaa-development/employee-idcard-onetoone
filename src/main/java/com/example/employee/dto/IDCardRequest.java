package com.example.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record IDCardRequest(
        @NotBlank @Size(max = 30) String cardNumber,
        @NotNull @PastOrPresent LocalDate issueDate) {
}

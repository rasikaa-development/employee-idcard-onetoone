package com.example.employee.dto;

import com.example.employee.entity.Employee;
import com.example.employee.entity.IDCard;
import java.time.LocalDate;

/** An ID card viewed from the card side: shows the one employee it belongs to. */
public record IDCardDetailsResponse(Long cardId, String cardNumber, LocalDate issueDate, EmployeeSummary employee) {

    public record EmployeeSummary(Long employeeId, String employeeName, String department) {
    }

    public static IDCardDetailsResponse from(IDCard card) {
        Employee owner = card.getEmployee();
        EmployeeSummary summary = owner == null ? null
                : new EmployeeSummary(owner.getEmployeeId(), owner.getEmployeeName(), owner.getDepartment());
        return new IDCardDetailsResponse(card.getCardId(), card.getCardNumber(), card.getIssueDate(), summary);
    }
}

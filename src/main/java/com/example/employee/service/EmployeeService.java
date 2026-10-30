package com.example.employee.service;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.entity.Employee;
import com.example.employee.entity.IDCard;
import com.example.employee.exception.DuplicateResourceException;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.repository.EmployeeRepository;
import com.example.employee.repository.IDCardRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final IDCardRepository idCardRepository;

    public EmployeeService(EmployeeRepository employeeRepository, IDCardRepository idCardRepository) {
        this.employeeRepository = employeeRepository;
        this.idCardRepository = idCardRepository;
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        String cardNumber = request.idCard().cardNumber();
        if (idCardRepository.existsByCardNumber(cardNumber)) {
            throw new DuplicateResourceException("ID card number '" + cardNumber + "' is already assigned to another employee");
        }
        Employee employee = new Employee(request.employeeName(), request.department());
        employee.setIdCard(new IDCard(cardNumber, request.idCard().issueDate()));
        // cascade = ALL: inserts the id_cards row first, then the employees row pointing to it
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream().map(EmployeeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long employeeId) {
        return EmployeeResponse.from(findEmployee(employeeId));
    }

    public EmployeeResponse updateEmployee(Long employeeId, EmployeeRequest request) {
        Employee employee = findEmployee(employeeId);
        IDCard card = employee.getIdCard();
        String cardNumber = request.idCard().cardNumber();
        if (idCardRepository.existsByCardNumberAndCardIdNot(cardNumber, card.getCardId())) {
            throw new DuplicateResourceException("ID card number '" + cardNumber + "' is already assigned to another employee");
        }
        employee.setEmployeeName(request.employeeName());
        employee.setDepartment(request.department());
        // update the existing card in place, so the employee keeps exactly one card (same cardId)
        card.setCardNumber(cardNumber);
        card.setIssueDate(request.idCard().issueDate());
        return EmployeeResponse.from(employee); // dirty checking issues the UPDATEs on commit
    }

    public void deleteEmployee(Long employeeId) {
        // cascade = ALL: the employee's ID card row is deleted as well
        employeeRepository.delete(findEmployee(employeeId));
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + employeeId));
    }
}

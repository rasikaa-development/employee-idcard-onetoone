package com.example.employee.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * OWNING side of the Employee <-> IDCard one-to-one association.
 * <p>
 * The foreign key {@code employees.card_id} lives in this table:
 * <ul>
 *   <li>NOT NULL -> every employee has exactly one ID card</li>
 *   <li>UNIQUE   -> an ID card can belong to only one employee</li>
 * </ul>
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @Column(nullable = false, length = 50)
    private String department;

    // cascade = ALL      -> saving / deleting an employee also saves / deletes the card
    // orphanRemoval=true -> a card can never exist without its employee
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private IDCard idCard;

    protected Employee() {
        // required by JPA
    }

    public Employee(String employeeName, String department) {
        this.employeeName = employeeName;
        this.department = department;
    }

    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public IDCard getIdCard() { return idCard; }

    /** Keeps both sides of the bidirectional association in sync. */
    public void setIdCard(IDCard idCard) {
        this.idCard = idCard;
        if (idCard != null) {
            idCard.setEmployee(this);
        }
    }
}

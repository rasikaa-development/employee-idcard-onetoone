package com.example.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * INVERSE side of the Employee <-> IDCard one-to-one association.
 * Stored in its own table; {@code mappedBy = "idCard"} means there is no FK column
 * here - the relationship is owned by {@link Employee#getIdCard()}.
 */
@Entity
@Table(name = "id_cards")
public class IDCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @Column(name = "card_number", nullable = false, unique = true, length = 30)
    private String cardNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @OneToOne(mappedBy = "idCard")
    private Employee employee;

    protected IDCard() {
        // required by JPA
    }

    public IDCard(String cardNumber, LocalDate issueDate) {
        this.cardNumber = cardNumber;
        this.issueDate = issueDate;
    }

    public Long getCardId() { return cardId; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public Employee getEmployee() { return employee; }

    /** Package-private: use {@link Employee#setIdCard(IDCard)} so both sides stay in sync. */
    void setEmployee(Employee employee) { this.employee = employee; }
}

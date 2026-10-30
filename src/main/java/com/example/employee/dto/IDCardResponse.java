package com.example.employee.dto;

import com.example.employee.entity.IDCard;
import java.time.LocalDate;

public record IDCardResponse(Long cardId, String cardNumber, LocalDate issueDate) {

    public static IDCardResponse from(IDCard card) {
        return new IDCardResponse(card.getCardId(), card.getCardNumber(), card.getIssueDate());
    }
}

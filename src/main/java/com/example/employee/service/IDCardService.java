package com.example.employee.service;

import com.example.employee.dto.IDCardDetailsResponse;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.repository.IDCardRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only access from the card side. Cards are created, updated and deleted only
 * through their employee, so a card can never exist without exactly one employee.
 */
@Service
@Transactional(readOnly = true)
public class IDCardService {

    private final IDCardRepository idCardRepository;

    public IDCardService(IDCardRepository idCardRepository) {
        this.idCardRepository = idCardRepository;
    }

    public List<IDCardDetailsResponse> getAllIdCards() {
        return idCardRepository.findAll().stream().map(IDCardDetailsResponse::from).toList();
    }

    public IDCardDetailsResponse getIdCard(Long cardId) {
        return idCardRepository.findById(cardId)
                .map(IDCardDetailsResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("ID card not found with id " + cardId));
    }
}

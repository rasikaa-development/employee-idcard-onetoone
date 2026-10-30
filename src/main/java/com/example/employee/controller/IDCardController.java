package com.example.employee.controller;

import com.example.employee.dto.IDCardDetailsResponse;
import com.example.employee.service.IDCardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Navigates the association from the card side (IDCard -> Employee). */
@RestController
@RequestMapping("/idcards")
public class IDCardController {

    private final IDCardService idCardService;

    public IDCardController(IDCardService idCardService) {
        this.idCardService = idCardService;
    }

    @GetMapping
    public List<IDCardDetailsResponse> getAllIdCards() {
        return idCardService.getAllIdCards();
    }

    @GetMapping("/{cardId}")
    public IDCardDetailsResponse getIdCard(@PathVariable Long cardId) {
        return idCardService.getIdCard(cardId);
    }
}

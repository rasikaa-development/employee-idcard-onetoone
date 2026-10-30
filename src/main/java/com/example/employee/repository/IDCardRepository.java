package com.example.employee.repository;

import com.example.employee.entity.IDCard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDCardRepository extends JpaRepository<IDCard, Long> {

    boolean existsByCardNumber(String cardNumber);

    boolean existsByCardNumberAndCardIdNot(String cardNumber, Long cardId);

    // load the owning employee in the same query
    @Override
    @EntityGraph(attributePaths = "employee")
    List<IDCard> findAll();

    @Override
    @EntityGraph(attributePaths = "employee")
    Optional<IDCard> findById(Long cardId);
}

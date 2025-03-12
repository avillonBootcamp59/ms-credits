package com.bank.pe.mscredit.entity;

import lombok.Data;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "credits")
public class Credit {
    @BsonId
    private String id;
    private String customerId; // Relación con Cliente
    private Double amount; //monto
    private String creditType; // "personal", "empresarial", "tarjeta de crédito"
    private Double interestRate; // tasa de interés
    private Double creditLimit; // límite de crédito
    private Double currentDebt; // deuda actual
    private Double availableLimit; //límite disponible
    private LocalDate dueDate; // fecha de vencimiento
    private Double outstandingAmount; // deuda pendiente
    private LocalDateTime createdAt; // Fecha de adquisición del crédito

    public Credit() {
        this.createdAt = LocalDateTime.now();
    }


}

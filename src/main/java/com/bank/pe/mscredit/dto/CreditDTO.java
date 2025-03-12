package com.bank.pe.mscredit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditDTO {
    private String id;
    private String customerId; // Relación con Cliente
    private String creditType; // "personal", "Empresarial"
    private Double amount; // Monto del crédito
    private Double creditLimit; // Límite del crédito
    private Double interestRate;  // Tasa de interés aplicada
}

package com.bank.pe.mscredit.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreditDTOTest {

    private CreditDTO creditDTO1;
    private CreditDTO creditDTO2;

    @BeforeEach
    void setUp() {
        creditDTO1 = new CreditDTO();
        creditDTO1.setId("1");
        creditDTO1.setCreditLimit(100.0);
        creditDTO1.setAmount(1500.0);
        creditDTO1.setCreditType("Personal");
        creditDTO1.setInterestRate(2.5);
        creditDTO1.setCustomerId("123");

        creditDTO2 = new CreditDTO();
        creditDTO2.setId("2");
        creditDTO2.setCreditLimit(1000.0);
        creditDTO2.setAmount(2600.0);
        creditDTO2.setCreditType("Empresarial");
        creditDTO2.setInterestRate(5.5);
    }

    @Test
    void testGettersAndSetters() {
        CreditDTO credit = new CreditDTO();
        credit.setId("10");
        credit.setCustomerId("999");
        credit.setCreditType("Mortgage");
        credit.setAmount(250000.0);
        credit.setInterestRate(3.75);

        assertThat(credit.getId()).isEqualTo("10");
        assertThat(credit.getCustomerId()).isEqualTo("999");
        assertThat(credit.getCreditType()).isEqualTo("Mortgage");
        assertThat(credit.getAmount()).isEqualTo(250000.0);
        assertThat(credit.getInterestRate()).isEqualTo(3.75);
    }

    @Test
    void testEqualsAndHashCode() {
        CreditDTO creditCopy = new CreditDTO();
        creditCopy.setId("1");
        creditCopy.setCreditLimit(100.0);
        creditCopy.setAmount(1500.0);
        creditCopy.setCreditType("Personal");
        creditCopy.setInterestRate(2.5);
        creditCopy.setCustomerId("123");

        assertThat(creditDTO1).isEqualTo(creditCopy);
        assertThat(creditDTO1.hashCode()).isEqualTo(creditCopy.hashCode());

        assertThat(creditDTO1).isNotEqualTo(creditDTO2);
    }

    @Test
    void testToString() {
        String expected = "CreditDTO(id=1, customerId=123, creditType=Personal, amount=1500.0, creditLimit=100.0, interestRate=2.5)";
        assertThat(creditDTO1.toString()).isEqualTo(expected);
    }

    @Test
    void testCanEqual() {
        assertThat(creditDTO1.canEqual(creditDTO2)).isTrue();
        assertThat(creditDTO1.canEqual(new Object())).isFalse();
    }
}

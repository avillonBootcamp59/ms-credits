package com.bank.pe.mscredit.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import com.bank.pe.mscredit.dto.CreditDTO;
import com.bank.pe.mscredit.entity.Credit;
import com.bank.pe.mscredit.service.CreditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

class CreditControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private CreditService creditService;

    @InjectMocks
    private CreditController creditController;

    private Credit credit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(creditController).build();
        credit = new Credit();
        credit.setId("4");
        credit.setCustomerId("123");
        credit.setAmount(100.0);
        credit.setCreditType("personal");
        credit.setInterestRate(2.6);
        credit.setCreditLimit(150.0);
        credit.setCurrentDebt(0.0);

    }

    @Test
    void testGetAllCredits() {
        when(creditService.listCredits()).thenReturn(Flux.just(credit));

        webTestClient.get().uri("/api/v1/credits")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Credit.class)
                .hasSize(1);
    }

    @Test
    void testGetCreditById_Found() {
        when(creditService.getCredit("1")).thenReturn(Mono.just(credit));

        webTestClient.get().uri("/api/v1/credits/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Credit.class)
                .isEqualTo(credit);
    }

    @Test
    void testGetCreditById_NotFound() {
        when(creditService.getCredit("999")).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/credits/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateCredit_Success() {
        Credit credit = new Credit();
        credit.setId("4");
        credit.setCustomerId("123");
        credit.setAmount(100.0);
        credit.setCreditType("personal");
        credit.setInterestRate(2.6);
        credit.setCreditLimit(150.0);
        credit.setCurrentDebt(0.0);
        when(creditService.createCredit(any(Credit.class))).thenReturn(Mono.just(credit));

        webTestClient.post().uri("/api/v1/credits")
                .contentType(APPLICATION_JSON)
                .bodyValue(credit) // Enviar un objeto Credit válido
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .value(response -> {
                    assert response.containsKey("message");
                    assert response.get("message").equals("Crédito creado exitosamente");
                });
    }

    @Test
    void testCreateCredit_NullCustomerId_ShouldReturnBadRequest() {
        credit.setCustomerId(null);
        webTestClient.post().uri("/api/v1/credits")
                .contentType(APPLICATION_JSON)
                .bodyValue(credit)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .value(response -> {
                    assert response.containsKey("error");
                    assert response.get("error").equals("Datos inválidos");
                });
    }

    @Test
    void testUpdateCredit_Success() {
        Credit updatedCredit = credit;
        when(creditService.updateCredit(eq("1"), any(Credit.class))).thenReturn(Mono.just(updatedCredit));

        webTestClient.put().uri("/api/v1/credits/1")
                .contentType(APPLICATION_JSON)
                .bodyValue(updatedCredit)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Credit.class)
                .isEqualTo(updatedCredit);
    }

    @Test
    void testUpdateCredit_NotFound() {
        when(creditService.updateCredit(eq("999"), any(Credit.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/api/v1/credits/999")
                .contentType(APPLICATION_JSON)
                .bodyValue(new Credit())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteCredit_Success() {
        when(creditService.deleteCredit("1")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/credits/1")
                .exchange()
                .expectStatus().isOk();
    }

  //  @Test
    void testDeleteCredit_NotFound() {
        when(creditService.deleteCredit("999")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/credits/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetCreditProductsByCustomer_Success() {
        CreditDTO creditDTO = new CreditDTO();
        creditDTO.setId("1");
        creditDTO.setCreditLimit(100.0);
        creditDTO.setAmount(1500.0);
        creditDTO.setCreditType("Personal");
        creditDTO.setInterestRate(2.5);
        creditDTO.setCustomerId("123");
        when(creditService.getCreditProductsByCustomer("123")).thenReturn(Flux.just(credit));

        webTestClient.get().uri("/api/v1/credits/customer/123")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CreditDTO.class)
                .hasSize(1);
    }

    @Test
    void testHasOverdueDebt() {
        when(creditService.hasOverdueDebt("123")).thenReturn(Mono.just(true));

        webTestClient.get().uri("/api/v1/credits/hasOverdueDebt/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }
}

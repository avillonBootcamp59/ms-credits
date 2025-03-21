package com.bank.pe.mscredit.service.impl;
import static org.mockito.Mockito.*;

import com.bank.pe.mscredit.client.CustomerClient;
import com.bank.pe.mscredit.dto.CustomerDTO;
import com.bank.pe.mscredit.entity.Credit;
import com.bank.pe.mscredit.repository.CreditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

class CreditServiceImplTest {

    @Mock
    private CreditRepository creditRepository;

    @InjectMocks
    private CreditServiceImpl creditService;

    @Mock
    private CustomerClient customerClient;

    private Credit credit1;
    private Credit credit2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        credit1 = new Credit();
        credit1.setId("1");
        credit1.setCustomerId("123");
        credit1.setAmount(100.0);
        credit1.setCreditType("personal");
        credit1.setInterestRate(5.5);
        credit1.setCreditLimit(150.0);
        credit1.setCurrentDebt(0.0);

        credit2 = new Credit();
        credit2.setId("1");
        credit2.setCustomerId("123");
        credit2.setAmount(5000.0);
        credit2.setCreditType("Business");
        credit2.setInterestRate(5.5);
        credit2.setCreditLimit(150.0);
        credit2.setCurrentDebt(0.0);


    }

    @Test
    void testListCredits() {
        when(creditRepository.findAll()).thenReturn(Flux.just(credit1, credit2));

        StepVerifier.create(creditService.listCredits())
                .expectNext(credit1)
                .expectNext(credit2)
                .verifyComplete();

        verify(creditRepository).findAll();
    }

    @Test
    void testGetCreditById_Found() {
        when(creditRepository.findById("1")).thenReturn(Mono.just(credit1));

        StepVerifier.create(creditService.getCredit("1"))
                .expectNext(credit1)
                .verifyComplete();

        verify(creditRepository).findById("1");
    }

    @Test
    void testGetCreditById_NotFound() {
        when(creditRepository.findById("999")).thenReturn(Mono.empty());

        StepVerifier.create(creditService.getCredit("999"))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatus().equals(HttpStatus.NOT_FOUND) &&
                                "Crédito no encontrado".equals(((ResponseStatusException) throwable).getReason())
                )
                .verify();

        verify(creditRepository).findById("999");
    }

    @Test
    void testCreateCredit() {
        Credit credit1 = new Credit();
        credit1.setId("4");
        credit1.setCustomerId("123");
        credit1.setAmount(100.0);
        credit1.setCreditType("personal");
        credit1.setInterestRate(2.6);
        credit1.setCreditLimit(150.0);
        credit1.setCurrentDebt(0.0);

        CustomerDTO customer  = new CustomerDTO();
        customer.setId("123");
        customer.setType("PERSONAL");
        when(customerClient.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(creditRepository.findByCustomerId("123")).thenReturn(Flux.empty());
        when(creditRepository.save(any(Credit.class))).thenReturn(Mono.just(credit1));

        StepVerifier.create(creditService.createCredit(credit1))
                .expectNext(credit1)
                .verifyComplete();

        verify(customerClient).getCustomerById("123");
        verify(creditRepository).save(any(Credit.class));
    }

    @Test
    public void testCreateCredit_NullCredit() {
        Mono<Credit> result = creditService.createCredit(null);

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void testCreateCredit_NullCustomerId() {
        Credit credit = new Credit();

        Mono<Credit> result = creditService.createCredit(credit);

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void testCreateCredit_CustomerNotFound() {
        Credit credit = new Credit();
        credit.setCustomerId("12345");

        when(customerClient.getCustomerById(credit.getCustomerId())).thenReturn(Mono.empty());

        Mono<Credit> result = creditService.createCredit(credit);

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void testCreateCredit_ValidPersonalCustomer() {
        Credit credit = new Credit();
        credit.setCustomerId("12345");
        CustomerDTO customer = new CustomerDTO();
        customer.setType("PERSONAL");

        when(customerClient.getCustomerById(credit.getCustomerId())).thenReturn(Mono.just(customer));
        when(creditRepository.findByCustomerId(credit.getCustomerId())).thenReturn(Flux.empty());
        when(creditRepository.save(credit)).thenReturn(Mono.just(credit));

        Mono<Credit> result = creditService.createCredit(credit);

        StepVerifier.create(result)
                .expectNext(credit)
                .verifyComplete();
    }

    @Test
    public void testCreateCredit_ValidEmpresarialCustomer() {
        Credit credit = new Credit();
        credit.setCustomerId("12345");
        CustomerDTO customer = new CustomerDTO();
        customer.setType("EMPRESARIAL");

        when(customerClient.getCustomerById(credit.getCustomerId())).thenReturn(Mono.just(customer));
        when(creditRepository.findByCustomerId(credit.getCustomerId())).thenReturn(Flux.empty());
        when(creditRepository.save(credit)).thenReturn(Mono.just(credit));

        Mono<Credit> result = creditService.createCredit(credit);

        StepVerifier.create(result)
                .expectNext(credit)
                .verifyComplete();
    }

    @Test
    public void testCreateCredit_InvalidCustomerType() {
        Credit credit = new Credit();
        credit.setCustomerId("12345");
        CustomerDTO customer = new CustomerDTO();
        customer.setType("INVALID");

        when(customerClient.getCustomerById(credit.getCustomerId())).thenReturn(Mono.just(customer));

        Mono<Credit> result = creditService.createCredit(credit);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testUpdateCredit_Success() {
        when(creditRepository.findById("1")).thenReturn(Mono.just(credit1));
        when(creditRepository.save(any(Credit.class))).thenReturn(Mono.just(credit1));

        StepVerifier.create(creditService.updateCredit("1", credit1))
                .expectNext(credit1)
                .verifyComplete();

        verify(creditRepository).findById("1");
        verify(creditRepository).save(any(Credit.class));
    }

    @Test
    void testUpdateCredit_NotFound() {
        when(creditRepository.findById("999")).thenReturn(Mono.empty());

        StepVerifier.create(creditService.updateCredit("999", credit1))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatus().equals(HttpStatus.NOT_FOUND) &&
                                "Cuenta bancaria no encontrada".equals(((ResponseStatusException) throwable).getReason())
                )
                .verify();

        verify(creditRepository).findById("999");
    }

    @Test
    void testDeleteCredit_Success() {
        when(creditRepository.findById("1")).thenReturn(Mono.just(credit1));
        when(creditRepository.delete(credit1)).thenReturn(Mono.empty());

        StepVerifier.create(creditService.deleteCredit("1"))
                .verifyComplete();

        verify(creditRepository).findById("1");
        verify(creditRepository).delete(credit1);
    }

    @Test
    void testDeleteCredit_NotFound() {
        when(creditRepository.findById("999")).thenReturn(Mono.empty());

        StepVerifier.create(creditService.deleteCredit("999"))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatus().equals(HttpStatus.NOT_FOUND) &&
                                "Crédito no encontrado".equals(((ResponseStatusException) throwable).getReason())
                )
                .verify();

        verify(creditRepository).findById("999");
    }

    @Test
    public void testHasOverdueDebt() {
        String customerId = "12345";
        Credit overdueCredit = new Credit();
        overdueCredit.setDueDate(LocalDate.now().minusDays(1));
        overdueCredit.setOutstandingAmount(100.0);

        when(creditRepository.findByCustomerId(customerId)).thenReturn(Flux.just(overdueCredit));

        Mono<Boolean> result = creditService.hasOverdueDebt(customerId);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testHasNoOverdueDebt() {
        String customerId = "12345";
        Credit nonOverdueCredit = new Credit();
        nonOverdueCredit.setDueDate(LocalDate.now().plusDays(1));
        nonOverdueCredit.setOutstandingAmount(100.0);

        when(creditRepository.findByCustomerId(customerId)).thenReturn(Flux.just(nonOverdueCredit));

        Mono<Boolean> result = creditService.hasOverdueDebt(customerId);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    public void testHasNoOutstandingAmount() {
        String customerId = "12345";
        Credit creditWithNoOutstandingAmount = new Credit();
        creditWithNoOutstandingAmount.setDueDate(LocalDate.now().minusDays(1));
        creditWithNoOutstandingAmount.setOutstandingAmount(0.0);

        when(creditRepository.findByCustomerId(customerId)).thenReturn(Flux.just(creditWithNoOutstandingAmount));

        Mono<Boolean> result = creditService.hasOverdueDebt(customerId);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testGetCreditProductsByCustomer_Success() {
        when(creditRepository.findByCustomerId("123")).thenReturn(Flux.just(credit1));

        StepVerifier.create(creditService.getCreditProductsByCustomer("123"))
                .expectNext(credit1)
                .verifyComplete();

        verify(creditRepository).findByCustomerId("123");
    }
}

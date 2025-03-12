package com.bank.pe.mscredit.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import com.bank.pe.mscredit.dto.CustomerDTO;
import com.bank.pe.mscredit.dto.CustomerResponse;
import reactor.core.publisher.Mono;

@Component
public class CustomerClient {

    private final WebClient webClient;

    public CustomerClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081/api/v1/customers").build();
    }

    public Mono<CustomerDTO> getCustomerById(String customerId) {
        return webClient.get()
                .uri("/getById/{id}", customerId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Cliente no encontrado")))
                .onStatus(HttpStatus::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Error en el servicio ms-customer")))
                .bodyToMono(CustomerResponse.class)
                .flatMap(response -> {
                    if (response.getData() == null || response.getData().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
                    }
                    return Mono.just(response.getData().get(0));
                });
    }
}

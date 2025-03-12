package com.bank.pe.mscredit.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.bank.pe.mscredit.dto.CreditDTO;
import com.bank.pe.mscredit.service.CreditService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.bank.pe.mscredit.entity.Credit;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/credits")
@RequiredArgsConstructor
@Tag(name = "Credit API", description = "Gestión de créditos bancarios")
public class CreditController {

    private static final Logger logger = LoggerFactory.getLogger(CreditController.class);
    private final CreditService creditService;

    @Operation(summary = "Obtener todos los créditos", description = "Lista todos los créditos otorgados por el banco")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de créditos obtenida correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public Flux<Credit> getAllCredits() {
        logger.info("Obteniendo todos los créditos bancarios");
        return creditService.listCredits();
    }

    @Operation(summary = "Obtener un crédito por ID")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Credit>> getCreditById(@PathVariable String id) {
        return creditService.getCredit(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registrar un nuevo crédito", description = "Registra un nuevo crédito en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Crédito creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error en la validación de la crédito"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
    })
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createCredit(@Valid @RequestBody Credit credit) {
        logger.info("Intentando crear crédito para cliente {}", credit.getCustomerId());
        return creditService.createCredit(credit)
                .map(savedCredit -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", "Crédito creado exitosamente", "id", savedCredit.getId())))
                .onErrorResume(ResponseStatusException.class, ex -> {
                    Map<String, String> errorResponse = Map.of(
                            "error", ex.getReason(),
                            "status", String.valueOf(ex.getRawStatusCode())
                    );
                    return Mono.just(ResponseEntity.status(ex.getRawStatusCode()).body(errorResponse));
                });
    }

    @Operation(summary = "Actualizar el monto de un crédito", description = "Modifica el monto de un crédito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monto de crédito actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Crédito no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Credit>> updateCredit(@PathVariable String id, @RequestBody Credit credit) {
        logger.info("Actualizando crédito id: {}", id);

        return creditService.updateCredit(id, credit)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar un crédito", description = "Elimina una crédito del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Crédito eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Crédito no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCredit(@PathVariable String id) {
        logger.info("Eliminando crédito con ID: {}", id);

        return creditService.deleteCredit(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener todos los créditos de un cliente", description = "Lista los créditos por cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de créditos obtenida correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/customer/{id}")
    public Flux<CreditDTO> getCreditProductsByCustomer(@PathVariable String id) {
        logger.info("Obteniendo todos los créditos bancarios del cliente con ID: {}", id);

        return creditService.getCreditProductsByCustomer(id)
                .map(this::convertToDTO)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontraron créditos para este cliente")));

    }

    @Operation(summary = "Obtener todos los créditos", description = "Lista todos los créditos otorgados por el banco")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de créditos obtenida correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/hasOverdueDebt/{id}")
    public Mono<Boolean> hasOverdueDebt(@PathVariable String id) {
        return creditService.hasOverdueDebt(id);
    }
    private CreditDTO convertToDTO(Credit credit) {
        return new CreditDTO(
                credit.getId(),
                credit.getCustomerId(),
                credit.getCreditType(),
                credit.getAmount(),
                credit.getCreditLimit(),
                credit.getInterestRate());
    }
}

package proyecto1.mscredit.rest;

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
import proyecto1.mscredit.service.CreditService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import proyecto1.mscredit.entity.Credit;
import proyecto1.mscredit.repository.CreditRepository;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1.0/credits")
@RequiredArgsConstructor
@Tag(name = "Credit API", description = "Gestión de créditos bancarios")
public class CreditController {

    private static final Logger logger = LoggerFactory.getLogger(CreditController.class);
    private final CreditRepository repository;
    private final CreditService creditService;

    @Operation(summary = "Obtener todos los créditos", description = "Lista todos los créditos otorgados por el banco")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de créditos obtenida correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public Flux<Credit> getAllCredits() {
        logger.info("Obteniendo todos los créditos bancarios");
        return repository.findAll();
    }

    @Operation(summary = "Obtener un crédito por ID")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Credit>> getCreditById(@PathVariable String id) {
        return repository.findById(id)
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
    public Mono<ResponseEntity<Credit>> createCredit(@Valid @RequestBody Credit credit) {
        logger.info("Intentando crear crédito para cliente {}", credit.getCustomerId());

        return creditService.validateAndCreateCredit(credit)
                .map(savedCredit -> ResponseEntity.status(HttpStatus.CREATED).body(savedCredit))
                .onErrorResume(error -> {
                    logger.error("Error al crear crédito: {}", error.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(null));
                });
    }

    @Operation(summary = "Actualizar el monto de un crédito", description = "Modifica el monto de un crédito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monto de crédito actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Crédito no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Credit>> updateCredit(@PathVariable String id, @RequestBody Double newAmount) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setAmount(newAmount);
                    return repository.save(existing);
                })
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
        return repository.findById(id)
                .flatMap(existing -> repository.delete(existing)
                        .then(Mono.just(ResponseEntity.ok().<Void>build())))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}

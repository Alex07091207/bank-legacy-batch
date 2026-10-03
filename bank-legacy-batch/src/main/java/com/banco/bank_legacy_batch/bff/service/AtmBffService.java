package com.banco.bank_legacy_batch.bff.service;

import com.banco.bank_legacy_batch.bff.dto.AtmRetiroRequest;
import com.banco.bank_legacy_batch.bff.dto.AtmRetiroResponse;
import com.banco.bank_legacy_batch.bff.dto.AtmSaldoResponse;
import com.banco.bank_legacy_batch.bff.repository.CuentaBffRepository;
import com.banco.bank_legacy_batch.model.Interes;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AtmBffService {

    private final CuentaBffRepository cuentaBffRepository;
    private final KafkaTemplate<String, String> kafkaTemplate; // Inyección de Kafka

    public AtmBffService(CuentaBffRepository cuentaBffRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.cuentaBffRepository = cuentaBffRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "metodoDeRespaldo")
    public AtmSaldoResponse consultarSaldo(Long cuentaId) {
        Interes cuenta = cuentaBffRepository.buscarCuenta(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));
        return new AtmSaldoResponse(cuenta.getSaldo());
    }

    public AtmSaldoResponse metodoDeRespaldoSaldo(Long cuentaId, Throwable ex) {
        System.out.println("Circuit Breaker activado (Saldo). Error: " + ex.getMessage());
        return new AtmSaldoResponse(BigDecimal.ZERO);
    }
    
    @Transactional
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "metodoDeRespaldoRetiro")
    public AtmRetiroResponse realizarRetiro(Long cuentaId, AtmRetiroRequest request) {
        Interes cuenta = cuentaBffRepository.buscarCuenta(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));

        if (request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a cero.");
        }

        if (cuenta.getSaldo().compareTo(request.getMonto()) < 0) {
            throw new IllegalStateException("Saldo insuficiente en la cuenta.");
        }

        BigDecimal nuevoSaldo = cuenta.getSaldo().subtract(request.getMonto());
        cuentaBffRepository.actualizarSaldo(cuentaId, nuevoSaldo);

        // Enviar mensaje asíncrono a Kafka
        String mensajeEvento = "Retiro de " + request.getMonto() + " procesado exitosamente para la cuenta " + cuentaId;
        kafkaTemplate.send("transacciones-topic", mensajeEvento);

        return new AtmRetiroResponse(cuentaId, request.getMonto(), nuevoSaldo, "EXITOSO", "Retiro realizado con exito.");
    }

    public AtmRetiroResponse metodoDeRespaldoRetiro(Long cuentaId, AtmRetiroRequest request, Throwable ex) {
        System.out.println("Circuit Breaker activado (Retiro). Error: " + ex.getMessage());
        return new AtmRetiroResponse(cuentaId, request.getMonto(), BigDecimal.ZERO, "FALLIDO", "Servicio no disponible. Intente mas tarde.");
    }
}
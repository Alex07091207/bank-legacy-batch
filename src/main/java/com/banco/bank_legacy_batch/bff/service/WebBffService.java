package com.banco.bank_legacy_batch.bff.service;

import org.springframework.stereotype.Service;
import com.banco.bank_legacy_batch.bff.dto.WebCuentaResponse;
import com.banco.bank_legacy_batch.bff.repository.CuentaBffRepository;
import com.banco.bank_legacy_batch.model.Interes;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;

@Service
public class WebBffService {
    private final CuentaBffRepository cuentaBffRepository;

    public WebBffService(CuentaBffRepository cuentaBffRepository) {
        this.cuentaBffRepository = cuentaBffRepository;
    }

    // 1. Agregamos la anotación que vigila este método
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "fallbackConsultarCuenta")
    public WebCuentaResponse consultarCuenta(Long cuentaId) {
        Interes cuenta = cuentaBffRepository.buscarCuenta(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));
        return new WebCuentaResponse(
                cuenta.getNombre(), 
                cuenta.getSaldo(), 
                cuenta.getTipo(), 
                cuenta.getSaldoFinal(), 
                cuenta.getEstado()
        );
    }

    // 2. Creamos el método de contingencia (Fallback) que se ejecutará si el método de arriba falla
    public WebCuentaResponse fallbackConsultarCuenta(Long cuentaId, Throwable throwable) {
        return new WebCuentaResponse(
                "Servicio Degradado", 
                BigDecimal.ZERO, 
                "N/A", 
                BigDecimal.ZERO, 
                "SISTEMA EN CONTINGENCIA"
        );
    }
}
package com.banco.bank_legacy_batch.bff.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.banco.bank_legacy_batch.bff.dto.AtmRetiroRequest;
import com.banco.bank_legacy_batch.bff.dto.AtmRetiroResponse;
import com.banco.bank_legacy_batch.bff.dto.AtmSaldoResponse;
import com.banco.bank_legacy_batch.bff.repository.CuentaBffRepository;
import com.banco.bank_legacy_batch.model.Interes;
import java.math.BigDecimal;

@Service
public class AtmBffService {
    private final CuentaBffRepository cuentaBffRepository;

    public AtmBffService(CuentaBffRepository cuentaBffRepository) {
        this.cuentaBffRepository = cuentaBffRepository;
    }

    public AtmSaldoResponse consultarSaldo(Long cuentaId) {
        Interes cuenta = cuentaBffRepository.buscarCuenta(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));
        return new AtmSaldoResponse(cuenta.getSaldo());
    }

    @Transactional
    public AtmRetiroResponse realizarRetiro(Long cuentaId, AtmRetiroRequest request) {
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a cero.");
        }
        Interes cuenta = cuentaBffRepository.buscarCuenta(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));
        if (cuenta.getSaldo().compareTo(request.getMonto()) < 0) {
            throw new IllegalStateException("Saldo insuficiente en la cuenta.");
        }
        BigDecimal nuevoSaldo = cuenta.getSaldo().subtract(request.getMonto());
        cuentaBffRepository.actualizarSaldo(cuentaId, nuevoSaldo);
        return new AtmRetiroResponse(cuentaId, request.getMonto(), nuevoSaldo, "EXITOSO", "Retiro realizado con exito.");
    }
}
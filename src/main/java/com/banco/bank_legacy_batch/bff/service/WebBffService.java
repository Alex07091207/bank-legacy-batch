package com.banco.bank_legacy_batch.bff.service;

import org.springframework.stereotype.Service;
import com.banco.bank_legacy_batch.bff.dto.WebCuentaResponse;
import com.banco.bank_legacy_batch.bff.repository.CuentaBffRepository;
import com.banco.bank_legacy_batch.model.Interes;

@Service
public class WebBffService {
    private final CuentaBffRepository cuentaBffRepository;

    public WebBffService(CuentaBffRepository cuentaBffRepository) {
        this.cuentaBffRepository = cuentaBffRepository;
    }

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
}
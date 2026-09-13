package com.banco.bank_legacy_batch.bff.service;

import org.springframework.stereotype.Service;
import com.banco.bank_legacy_batch.bff.dto.MobileCuentaResponse;
import com.banco.bank_legacy_batch.bff.repository.CuentaBffRepository;
import com.banco.bank_legacy_batch.model.Interes;

@Service
public class MobileBffService {
    private final CuentaBffRepository cuentaBffRepository;

    public MobileBffService(CuentaBffRepository cuentaBffRepository) {
        this.cuentaBffRepository = cuentaBffRepository;
    }

    public MobileCuentaResponse consultarCuenta(Long cuentaId) {
        Interes cuenta = cuentaBffRepository.buscarCuenta(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));
        return new MobileCuentaResponse(cuenta.getNombre(), cuenta.getSaldo());
    }
}
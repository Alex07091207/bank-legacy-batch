package com.banco.bank_legacy_batch.bff.service;

import org.springframework.stereotype.Service;

@Service
public class BffSecurityService {
    public void validarCanal(String canalEsperado, String canalRecibido) {
        if (canalRecibido == null || !canalEsperado.equalsIgnoreCase(canalRecibido)) {
            throw new RuntimeException("Acceso no autorizado para este canal");
        }
    }

    public void validarOperacion(String operacionEsperada, String operacionRecibida) {
        if (operacionRecibida == null || !operacionEsperada.equalsIgnoreCase(operacionRecibida)) {
            throw new RuntimeException("Operación no autorizada");
        }
    }
}
package com.banco.bank_legacy_batch.bff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.banco.bank_legacy_batch.bff.dto.MobileCuentaResponse;
import com.banco.bank_legacy_batch.bff.service.BffSecurityService;
import com.banco.bank_legacy_batch.bff.service.MobileBffService;

@RestController
@RequestMapping("/bff/mobile")
public class MobileBffController {
    private final MobileBffService mobileBffService;
    private final BffSecurityService securityService;

    public MobileBffController(MobileBffService mobileBffService, BffSecurityService securityService) {
        this.mobileBffService = mobileBffService;
        this.securityService = securityService;
    }

    @GetMapping("/cuenta/{cuentaId}")
    public MobileCuentaResponse consultarCuenta(
            @PathVariable Long cuentaId,
            @RequestHeader("X-Canal") String canal) {
        securityService.validarCanal("MOBILE", canal);
        return mobileBffService.consultarCuenta(cuentaId);
    }
}
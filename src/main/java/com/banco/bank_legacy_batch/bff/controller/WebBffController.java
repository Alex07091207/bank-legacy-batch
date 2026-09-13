package com.banco.bank_legacy_batch.bff.controller;

import org.springframework.web.bind.annotation.*;
import com.banco.bank_legacy_batch.bff.dto.WebCuentaResponse;
import com.banco.bank_legacy_batch.bff.service.BffSecurityService;
import com.banco.bank_legacy_batch.bff.service.WebBffService;

@RestController
@RequestMapping("/bff/web")
public class WebBffController {
    private final WebBffService webBffService;
    private final BffSecurityService securityService;

    public WebBffController(WebBffService webBffService, BffSecurityService securityService) {
        this.webBffService = webBffService;
        this.securityService = securityService;
    }

    @GetMapping("/cuenta/{cuentaId}")
    public WebCuentaResponse consultarCuenta(
            @PathVariable Long cuentaId,
            @RequestHeader("X-Canal") String canal) {
        securityService.validarCanal("WEB", canal);
        return webBffService.consultarCuenta(cuentaId);
    }
}
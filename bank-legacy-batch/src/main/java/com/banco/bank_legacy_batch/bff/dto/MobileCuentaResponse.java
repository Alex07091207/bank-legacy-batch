package com.banco.bank_legacy_batch.bff.dto;

import java.math.BigDecimal;

public class MobileCuentaResponse {

    private String nombre;
    private BigDecimal saldo;

    public MobileCuentaResponse() {}

    public MobileCuentaResponse(String nombre, BigDecimal saldo) {
        this.nombre = nombre;
        this.saldo = saldo;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

}

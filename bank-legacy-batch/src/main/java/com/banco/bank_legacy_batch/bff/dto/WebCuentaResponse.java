package com.banco.bank_legacy_batch.bff.dto;

import java.math.BigDecimal;

public class WebCuentaResponse {

    private String nombre;
    private BigDecimal saldo;
    private String tipo;
    private BigDecimal saldoFinal;
    private String estado;

    public WebCuentaResponse() {}

    public WebCuentaResponse(String nombre, BigDecimal saldo, String tipo, BigDecimal saldoFinal, String estado) {
        this.nombre = nombre;
        this.saldo = saldo;
        this.tipo = tipo;
        this.saldoFinal = saldoFinal;
        this.estado = estado;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(BigDecimal saldoFinal) { this.saldoFinal = saldoFinal; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

}

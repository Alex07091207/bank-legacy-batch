package com.banco.bank_legacy_batch.bff.dto;

import java.math.BigDecimal;

public class AtmRetiroResponse {
    private Long cuentaId;
    private BigDecimal montoRetirado;
    private BigDecimal saldoRestante;
    private String estado;
    private String mensaje;

    public AtmRetiroResponse() {}

    public AtmRetiroResponse(Long cuentaId, BigDecimal montoRetirado, BigDecimal saldoRestante, String estado, String mensaje) {
        this.cuentaId = cuentaId;
        this.montoRetirado = montoRetirado;
        this.saldoRestante = saldoRestante;
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public Long getCuentaId() { return cuentaId; }
    public BigDecimal getMontoRetirado() { return montoRetirado; }
    public BigDecimal getSaldoRestante() { return saldoRestante; }
    public String getEstado() { return estado; }
    public String getMensaje() { return mensaje; }
}
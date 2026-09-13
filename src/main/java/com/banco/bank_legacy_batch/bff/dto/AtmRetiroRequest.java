package com.banco.bank_legacy_batch.bff.dto;

import java.math.BigDecimal;

public class AtmRetiroRequest {
    private BigDecimal monto;

    public AtmRetiroRequest() {}
    public AtmRetiroRequest(BigDecimal monto) { this.monto = monto; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
package com.banco.bank_legacy_batch.bff.dto;

import java.math.BigDecimal;

public class AtmSaldoResponse {

    private BigDecimal saldo;

    public AtmSaldoResponse() {}
    public AtmSaldoResponse(BigDecimal saldo) { this.saldo = saldo; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

}
    
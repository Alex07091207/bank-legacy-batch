package com.banco.bank_legacy_batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Transaccion {

    private Long cuentaId;
    private LocalDate fecha;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
    private String estado;

}
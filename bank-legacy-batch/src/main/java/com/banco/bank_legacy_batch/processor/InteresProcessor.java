package com.banco.bank_legacy_batch.processor;

import com.banco.bank_legacy_batch.model.Interes;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InteresProcessor
        implements ItemProcessor<Interes, Interes> {

    private static final BigDecimal TASA_AHORRO =
            new BigDecimal("0.02");

    private static final BigDecimal TASA_PRESTAMO =
            new BigDecimal("0.05");

    private static final BigDecimal TASA_HIPOTECA =
            new BigDecimal("0.04");

    @Override
    public Interes process(Interes item) {

        boolean tieneObservaciones = false;

        // Cuenta
        if (item.getCuentaId() == null ||
                item.getCuentaId() <= 0) {

            item.setCuentaId(0L);
            tieneObservaciones = true;
        }

        // Nombre
        if (item.getNombre() == null ||
                item.getNombre().trim().isEmpty()) {

            item.setNombre("Cliente no informado");
            tieneObservaciones = true;

        } else {

            item.setNombre(item.getNombre().trim());
        }

        // Saldo
        if (item.getSaldo() == null) {

            item.setSaldo(BigDecimal.ZERO);
            tieneObservaciones = true;

        } else if (item.getSaldo().signum() < 0) {

            item.setSaldo(item.getSaldo().abs());
            tieneObservaciones = true;
        }

        // Edad
        if (item.getEdad() == null) {

            item.setEdad(0);
            tieneObservaciones = true;

        } else if (item.getEdad() < 18 ||
                item.getEdad() > 100) {

            tieneObservaciones = true;
        }

        // Tipo
        String tipo;

        if (item.getTipo() == null ||
                item.getTipo().trim().isEmpty()) {

            tipo = "ahorro";
            tieneObservaciones = true;

        } else {

            tipo = item.getTipo().trim().toLowerCase();

            if (!tipo.equals("ahorro")
                    && !tipo.equals("prestamo")
                    && !tipo.equals("hipoteca")) {

                tipo = "ahorro";
                tieneObservaciones = true;
            }
        }

        item.setTipo(tipo);

        // Tasa según tipo de cuenta
        BigDecimal tasa;

        switch (tipo) {

            case "prestamo":
                tasa = TASA_PRESTAMO;
                break;

            case "hipoteca":
                tasa = TASA_HIPOTECA;
                break;

            case "ahorro":
            default:
                tasa = TASA_AHORRO;
                break;
        }

        // Cálculo del interés
        BigDecimal interes = item.getSaldo()
                .multiply(tasa)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal saldoFinal = item.getSaldo()
                .add(interes)
                .setScale(2, RoundingMode.HALF_UP);

        item.setInteres(interes);
        item.setSaldoFinal(saldoFinal);

        if (tieneObservaciones) {
            item.setEstado("PROCESADO_CON_OBSERVACIONES");
        } else {
            item.setEstado("PROCESADO");
        }

        return item;
    }
}
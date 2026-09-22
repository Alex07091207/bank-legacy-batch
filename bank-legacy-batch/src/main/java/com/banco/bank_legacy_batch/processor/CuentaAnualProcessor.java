package com.banco.bank_legacy_batch.processor;

import com.banco.bank_legacy_batch.model.CuentaAnual;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class CuentaAnualProcessor
        implements ItemProcessor<CuentaAnual, CuentaAnual> {

    @Override
    public CuentaAnual process(CuentaAnual item) {

        boolean tieneObservaciones = false;

        // ID
        if (item.getId() == null ||
                item.getId() <= 0) {

            item.setId(0L);
            tieneObservaciones = true;
        }

        // Fecha
        if (item.getFecha() == null) {
            tieneObservaciones = true;
        }

        // Monto
        if (item.getMonto() == null) {

            item.setMonto(BigDecimal.ZERO);
            tieneObservaciones = true;

        } else if (item.getMonto().signum() < 0) {

            item.setMonto(item.getMonto().abs());
            tieneObservaciones = true;
        }

        // Tipo
        if (item.getTipo() == null ||
                item.getTipo().trim().isEmpty()) {

            item.setTipo("desconocido");
            tieneObservaciones = true;

        } else {

            String tipo =
                    item.getTipo().trim().toLowerCase();

            if (!tipo.equals("debito")
                    && !tipo.equals("credito")) {

                tieneObservaciones = true;
            }

            item.setTipo(tipo);
        }

        if (tieneObservaciones) {
            item.setEstado("PROCESADO_CON_OBSERVACIONES");
        } else {
            item.setEstado("PROCESADO");
        }

        return item;
    }
}
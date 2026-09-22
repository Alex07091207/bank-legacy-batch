package com.banco.bank_legacy_batch.processor;

import java.math.BigDecimal;

import org.springframework.batch.item.ItemProcessor;

import com.banco.bank_legacy_batch.model.Transaccion;

public class TransaccionProcessor
        implements ItemProcessor<Transaccion, Transaccion> {

    @Override
    public Transaccion process(Transaccion item) {

        StringBuilder observaciones = new StringBuilder();

        // Cuenta
        if (item.getCuentaId() == null || item.getCuentaId() <= 0) {
            item.setCuentaId(0L);
            observaciones.append("Cuenta inválida; ");
        }

        // Fecha
        if (item.getFecha() == null) {
            observaciones.append("Fecha inválida; ");
        }

        // Monto
        if (item.getMonto() == null) {
            item.setMonto(BigDecimal.ZERO);
            observaciones.append("Monto vacío; ");
        } else if (item.getMonto().signum() < 0) {
            // Conservamos el registro y normalizamos el monto
            item.setMonto(item.getMonto().abs());
            observaciones.append("Monto negativo normalizado; ");
        } else if (item.getMonto().signum() == 0) {
            observaciones.append("Monto igual a cero; ");
        }

        // Tipo
        if (item.getTipo() == null ||
                item.getTipo().trim().isEmpty()) {

            item.setTipo("desconocido");
            observaciones.append("Tipo vacío; ");

        } else {

            String tipo = item.getTipo().trim().toLowerCase();

            if (!tipo.equals("deposito")
                    && !tipo.equals("retiro")
                    && !tipo.equals("compra")) {

                observaciones.append("Tipo no reconocido; ");
            }

            item.setTipo(tipo);
        }

        // Descripción
        if (item.getDescripcion() == null ||
                item.getDescripcion().trim().isEmpty()) {

            item.setDescripcion("Sin descripción");
            observaciones.append("Descripción vacía; ");

        } else {

            item.setDescripcion(
                    item.getDescripcion().trim()
            );
        }

        // Estado
        if (observaciones.length() == 0) {
            item.setEstado("PROCESADO");
        } else {
            item.setEstado(
                    "PROCESADO_CON_OBSERVACIONES"
            );
        }

        return item;
    }
}
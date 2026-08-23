package com.banco.bank_legacy_batch.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

public class CustomSkipPolicy implements SkipPolicy{

    private static final Logger logger = LoggerFactory.getLogger(CustomSkipPolicy.class);
    private static final int MAX_SKIP_COUNT = 10;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        // Tolerancia a fallos de lectura/parseo del CSV (ej. un tipo de dato incorrecto)
        if (t instanceof FlatFileParseException && skipCount < MAX_SKIP_COUNT) {
            logger.warn("Saltando línea con error de parseo. Error: {}", t.getMessage());
            return true; 
        }
        
        // Puede agregar más instancias, por ejemplo para errores de Base de Datos
        if (t instanceof Exception && skipCount < MAX_SKIP_COUNT) {
            logger.warn("Error genérico encontrado, saltando registro. Error: {}", t.getMessage());
            return true;
        }

        // Si supera el límite o es un error crítico que no queremos tolerar, no se salta (el Job falla)
        return false;
    }
}



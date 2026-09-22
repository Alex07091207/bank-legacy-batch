package com.banco.bank_legacy_batch.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import com.banco.bank_legacy_batch.Exception.DatoInvalidoException;

public class CustomSkipPolicy implements SkipPolicy {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomSkipPolicy.class);
    private static final int MAX_SKIP_COUNT = 10;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        // 1. Tolerancia a fallos de formato en el CSV
        if (t instanceof FlatFileParseException && skipCount < MAX_SKIP_COUNT) {
            logger.warn("Saltando línea con error de parseo. Error: {}", t.getMessage());
            return true; 
        }
        
        // 2. Tolerancia a tus propios errores de negocio
        if (t instanceof DatoInvalidoException && skipCount < MAX_SKIP_COUNT) {
            logger.warn("Error de negocio (Dato Inválido), saltando registro. Error: {}", t.getMessage());
            return true;
        }
        
        // Si es otro error grave (ej. se cayó el servidor), el Job debe fallar
        return false;
    }
}
package com.banco.bank_legacy_batch.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class CustomSkipListener implements SkipListener<Object, Object> {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        logger.warn("SkipListener - Error en lectura omitido: {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        logger.warn("SkipListener - Error en escritura omitido para item {}: {}", item, t.getMessage());
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        logger.warn("SkipListener - Error en procesamiento omitido para item {}: {}", item, t.getMessage());
    }
}

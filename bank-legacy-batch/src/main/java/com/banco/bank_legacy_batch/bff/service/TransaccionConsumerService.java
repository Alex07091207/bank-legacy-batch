package com.banco.bank_legacy_batch.bff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransaccionConsumerService {

    private static final Logger log = LoggerFactory.getLogger(TransaccionConsumerService.class);

    @KafkaListener(topics = "transacciones-topic", groupId = "bank-audit-group")
    public void consumirEventoTransaccion(String mensaje) {
        log.info("=== [NOTIFICACIÓN ASÍNCRONA KAFKA] ===");
        log.info("Registrando evento en auditoría: {}", mensaje);
    }
}
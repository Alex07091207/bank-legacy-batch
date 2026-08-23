package com.banco.bank_legacy_batch.config;

import com.banco.bank_legacy_batch.policy.CustomSkipPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchConfig {

    // Configuración de los 3 hilos de ejecución paralela
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // 3 hilos base
        executor.setMaxPoolSize(3);  // Máximo 3 hilos
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("Batch-Hilo-");
        executor.initialize();
        return executor;
    }

    // Registramos nuestra política personalizada
    @Bean
    public CustomSkipPolicy customSkipPolicy() {
        return new CustomSkipPolicy();
    }
}
package com.banco.bank_legacy_batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JobRunner implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("transaccionesJob")
    private Job transaccionesJob;

    @Autowired
    @Qualifier("interesesJob")
    private Job interesesJob;

    @Autowired
    @Qualifier("cuentasAnualesJob")
    private Job cuentasAnualesJob;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INICIANDO MIGRACIÓN BATCH CONTROLADA ===");

        // Generamos parámetros únicos basados en el tiempo para que Spring Batch ejecute los jobs como nuevas instancias
        JobParameters params = new JobParametersBuilder()
                .addLong("tiempoEjecucion", System.currentTimeMillis())
                .toJobParameters();

        System.out.println("--> 1. Iniciando Job de Transacciones...");
        jobLauncher.run(transaccionesJob, params);

        System.out.println("--> 2. Iniciando Job de Intereses...");
        jobLauncher.run(interesesJob, params);

        System.out.println("--> 3. Iniciando Job de Cuentas Anuales...");
        jobLauncher.run(cuentasAnualesJob, params);

        System.out.println("=== MIGRACION BATCH COMPLETADA CON ÉXITO ===");
    }
}
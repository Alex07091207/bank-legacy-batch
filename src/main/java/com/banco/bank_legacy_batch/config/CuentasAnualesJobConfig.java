package com.banco.bank_legacy_batch.config;

import com.banco.bank_legacy_batch.model.CuentaAnual;
import com.banco.bank_legacy_batch.policy.CustomSkipPolicy;
import com.banco.bank_legacy_batch.processor.CuentaAnualProcessor;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;

import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CuentasAnualesJobConfig {

@Bean
public FlatFileItemReader<CuentaAnual> cuentasAnualesReader() {
        return new FlatFileItemReaderBuilder<CuentaAnual>()
                .name("cuentasAnualesReader")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                // Leemos las 5 columnas exactas del CSV
                .names("cuenta_id", "fecha", "tipo", "monto", "descripcion")
                .saveState(false)
                .fieldSetMapper(fieldSet -> {
                CuentaAnual item = new CuentaAnual();
                
                    // Limpiamos la fecha por si viene con slashes (ej: 2024/10/01)
                String fechaStr = fieldSet.readString("fecha");
                if(fechaStr != null && fechaStr.contains("/")) {
                        fechaStr = fechaStr.replace("/", "-");
                }
                item.setFecha(LocalDate.parse(fechaStr));
                
                    // Asignamos el monto
                String montoStr = fieldSet.readString("monto");
                if (montoStr != null && !montoStr.trim().isEmpty()) {
                        item.setMonto(new BigDecimal(montoStr));
                }
                
                item.setTipo(fieldSet.readString("tipo"));
                return item;
                })
                .build();
}

@Bean
public CuentaAnualProcessor cuentaAnualProcessor() {
        return new CuentaAnualProcessor();
}

@Bean
public JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter(
        DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<CuentaAnual>()
                .dataSource(dataSource)
                .sql("""
                INSERT INTO cuentas_anuales_procesadas
                (
                        id,
                        fecha,
                        monto,
                        tipo,
                        estado
                )
                VALUES
                (
                        :id,
                        :fecha,
                        :monto,
                        :tipo,
                        :estado
                )
                """)
                .beanMapped()
                .build();
}

@Bean
public Step cuentasAnualesStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        FlatFileItemReader<CuentaAnual> cuentaAnualReader,
        CuentaAnualProcessor cuentaAnualProcessor,
        JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter,
        TaskExecutor taskExecutor,         
        CustomSkipPolicy customSkipPolicy  
        ) {

        return new StepBuilder(
                "cuentasAnualesStep",
                jobRepository)
                .<CuentaAnual, CuentaAnual>chunk(
                        5,                 
                        transactionManager)
                .reader(cuentaAnualReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)
                
                .taskExecutor(taskExecutor)    
                .faultTolerant()
                .skipPolicy(customSkipPolicy) 
                .build();
}

@Bean
public Job cuentasAnualesJob(
        JobRepository jobRepository,
        Step cuentasAnualesStep) {

        return new JobBuilder(
                "cuentasAnualesJob",
                jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cuentasAnualesStep)
                .build();
}
}
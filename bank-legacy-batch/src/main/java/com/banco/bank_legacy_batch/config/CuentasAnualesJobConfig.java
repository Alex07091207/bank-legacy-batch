package com.banco.bank_legacy_batch.config;

import com.banco.bank_legacy_batch.model.CuentaAnual;
import com.banco.bank_legacy_batch.partitioner.ArchivoPartitioner;
import com.banco.bank_legacy_batch.policy.CustomSkipListener;
import com.banco.bank_legacy_batch.policy.CustomSkipPolicy;
import com.banco.bank_legacy_batch.processor.CuentaAnualProcessor;

import org.springframework.dao.TransientDataAccessException;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CuentasAnualesJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<CuentaAnual> cuentasAnualesReader(
            @Value("#{stepExecutionContext['start']}") Integer start,
            @Value("#{stepExecutionContext['partitionSize']}") Integer partitionSize) {

        int linesToSkip = 1;
        if (start != null) linesToSkip = 1 + start;
        int maxCount = (partitionSize != null) ? partitionSize : 1000;

        return new FlatFileItemReaderBuilder<CuentaAnual>()
                .name("cuentasAnualesReader")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .linesToSkip(linesToSkip)
                .maxItemCount(maxCount)
                .delimited()
                .delimiter(",")
                .names("cuenta_id", "fecha", "tipo", "monto", "descripcion")
                .saveState(false)
                .fieldSetMapper(fieldSet -> {
                CuentaAnual item = new CuentaAnual();

                try { item.setCuentaId(fieldSet.readLong("cuenta_id")); } 
                catch (Exception e) { item.setCuentaId(0L); }

                    // Parseo BLINDADO de fecha
                String dateStr = fieldSet.readString("fecha");
                LocalDate parsedDate = null;
                if (dateStr != null && !dateStr.trim().isEmpty()) {
                        String[] formatos = {"yyyy-MM-dd", "dd-MM-yyyy", "yyyy/MM/dd", "dd/MM/yyyy"};
                        for (String formato : formatos) {
                        try {
                                parsedDate = LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern(formato));
                                break; 
                        } catch (Exception e) {}
                        }
                }
                item.setFecha(parsedDate);

                String montoStr = fieldSet.readString("monto");
                if (montoStr != null && !montoStr.trim().isEmpty()) {
                        try { item.setMonto(new BigDecimal(montoStr)); } catch (Exception e) {}
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
public JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<CuentaAnual>()
                .dataSource(dataSource)
                .sql("INSERT INTO cuentas_anuales_procesadas (cuenta_id, fecha, monto, tipo, estado) " +
                "VALUES (:cuentaId, :fecha, :monto, :tipo, :estado)")
                .beanMapped()
                .build();
}

@Bean
public Step cuentasAnualesMinionStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        FlatFileItemReader<CuentaAnual> cuentaAnualReader,
        CuentaAnualProcessor cuentaAnualProcessor,
        JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter,
        CustomSkipPolicy customSkipPolicy,
        CustomSkipListener customSkipListener) {

        return new StepBuilder("cuentasAnualesMinionStep", jobRepository)
                .<CuentaAnual, CuentaAnual>chunk(5, transactionManager) 
                .reader(cuentaAnualReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)
                .faultTolerant()
                .skipPolicy(customSkipPolicy)               
                .listener(customSkipListener)               
                .retryLimit(3)                              
                .retry(TransientDataAccessException.class)  
                .build();
}

@Bean
public Step cuentasAnualesPartitionStep(JobRepository jobRepository, Step cuentasAnualesMinionStep, TaskExecutor taskExecutor) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(cuentasAnualesMinionStep);
        partitionHandler.setTaskExecutor(taskExecutor); 
        partitionHandler.setGridSize(3);

        return new StepBuilder("cuentasAnualesPartitionStep", jobRepository)
                .partitioner("cuentasAnualesMinionStep", new ArchivoPartitioner(1000))
                .partitionHandler(partitionHandler)
                .build();
}

@Bean
public Job cuentasAnualesJob(JobRepository jobRepository, Step cuentasAnualesPartitionStep) {
        return new JobBuilder("cuentasAnualesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cuentasAnualesPartitionStep)
                .build();
}
}
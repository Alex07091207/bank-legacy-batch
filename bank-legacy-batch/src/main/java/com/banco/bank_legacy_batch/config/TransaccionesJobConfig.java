package com.banco.bank_legacy_batch.config;

import com.banco.bank_legacy_batch.model.Transaccion;
import com.banco.bank_legacy_batch.partitioner.ArchivoPartitioner;
import com.banco.bank_legacy_batch.policy.CustomSkipListener;
import com.banco.bank_legacy_batch.policy.CustomSkipPolicy;
import com.banco.bank_legacy_batch.processor.TransaccionProcessor;

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
public class TransaccionesJobConfig {

@Bean
@StepScope
public FlatFileItemReader<Transaccion> transaccionesReader(
        @Value("#{stepExecutionContext['start']}") Integer start,
        @Value("#{stepExecutionContext['partitionSize']}") Integer partitionSize) {

        int linesToSkip = 1; 
        if (start != null) linesToSkip = 1 + start;
        int maxCount = (partitionSize != null) ? partitionSize : 1000; // <--- AHORA LEE 1000

        return new FlatFileItemReaderBuilder<Transaccion>()
                .name("transaccionesReader")
                .resource(new ClassPathResource("data/transacciones.csv"))
                .linesToSkip(linesToSkip)
                .maxItemCount(maxCount)
                .delimited()
                .delimiter(",")
                .names("cuenta_id", "fecha", "monto", "tipo")
                .saveState(false)
                .fieldSetMapper(fieldSet -> {
                Transaccion item = new Transaccion();
                
                try { item.setCuentaId(fieldSet.readLong("cuenta_id")); } 
                catch (Exception e) { item.setCuentaId(0L); }

                    // Parseo robusto de múltiples formatos de fecha
                String dateStr = fieldSet.readString("fecha");
                LocalDate parsedDate = null;
                if (dateStr != null && !dateStr.trim().isEmpty()) {
                        String[] formatos = {"yyyy-MM-dd", "dd-MM-yyyy", "yyyy/MM/dd", "dd/MM/yyyy"};
                        for (String formato : formatos) {
                        try {
                                parsedDate = LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern(formato));
                                break; // Si funciona, sale del bucle
                        } catch (Exception e) {}
                        }
                }
                    item.setFecha(parsedDate); // Si falla, pasa como null y el Processor lo atrapa

                String montoStr = fieldSet.readString("monto");
                if (montoStr != null && !montoStr.trim().isEmpty()) {
                        try { item.setMonto(new BigDecimal(montoStr)); } catch (Exception e) {}
                }

                item.setTipo(fieldSet.readString("tipo"));
                item.setDescripcion("");
                return item;
                })
                .build();
}

@Bean
public TransaccionProcessor transaccionesProcessor() {
        return new TransaccionProcessor();
}

@Bean
public JdbcBatchItemWriter<Transaccion> transaccionesWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaccion>()
                .dataSource(dataSource)
                .sql("INSERT INTO transacciones_procesadas (cuenta_id, fecha, monto, tipo, descripcion, estado) " +
                "VALUES (:cuentaId, :fecha, :monto, :tipo, :descripcion, :estado)")
                .beanMapped()
                .build();
}

@Bean
public Step transaccionesMinionStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        FlatFileItemReader<Transaccion> transaccionesReader,
        TransaccionProcessor transaccionesProcessor,
        JdbcBatchItemWriter<Transaccion> transaccionesWriter,
        CustomSkipPolicy customSkipPolicy,
        CustomSkipListener customSkipListener) {

        return new StepBuilder("transaccionesMinionStep", jobRepository)
                .<Transaccion, Transaccion>chunk(5, transactionManager) 
                .reader(transaccionesReader)
                .processor(transaccionesProcessor)
                .writer(transaccionesWriter)
                .faultTolerant()
                .skipPolicy(customSkipPolicy)               
                .listener(customSkipListener)               
                .retryLimit(3)                              
                .retry(TransientDataAccessException.class)  
                .build();
}

@Bean
public Step transaccionesPartitionStep(JobRepository jobRepository, Step transaccionesMinionStep, TaskExecutor taskExecutor) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(transaccionesMinionStep);
        partitionHandler.setTaskExecutor(taskExecutor); 
        partitionHandler.setGridSize(3); 

        return new StepBuilder("transaccionesPartitionStep", jobRepository)
                .partitioner("transaccionesMinionStep", new ArchivoPartitioner(1000)) // <--- 1000 PARTICIONES
                .partitionHandler(partitionHandler)
                .build();
}

@Bean
public Job transaccionesJob(JobRepository jobRepository, Step transaccionesPartitionStep) {
        return new JobBuilder("transaccionesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(transaccionesPartitionStep) 
                .build();
}
}

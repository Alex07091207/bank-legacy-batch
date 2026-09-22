package com.banco.bank_legacy_batch.config;

import com.banco.bank_legacy_batch.model.Interes;
import com.banco.bank_legacy_batch.partitioner.ArchivoPartitioner;
import com.banco.bank_legacy_batch.policy.CustomSkipListener;
import com.banco.bank_legacy_batch.policy.CustomSkipPolicy;
import com.banco.bank_legacy_batch.processor.InteresProcessor;

import org.springframework.dao.TransientDataAccessException; 
import javax.sql.DataSource;
import java.math.BigDecimal;

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
public class InteresesJobConfig {

    @Bean
    @StepScope 
    public FlatFileItemReader<Interes> interesReader(
            @Value("#{stepExecutionContext['start']}") Integer start,
            @Value("#{stepExecutionContext['partitionSize']}") Integer partitionSize) {

        int linesToSkip = 1;
        if (start != null) linesToSkip = 1 + start;
        int maxCount = (partitionSize != null) ? partitionSize : 1000; // <--- 1000

        return new FlatFileItemReaderBuilder<Interes>()
                .name("interesReader")
                .resource(new ClassPathResource("data/intereses.csv"))
                .linesToSkip(linesToSkip)
                .maxItemCount(maxCount)
                .delimited()
                .delimiter(",")
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .saveState(false)
                .fieldSetMapper(fieldSet -> {
                    Interes item = new Interes();
                    
                    try { item.setCuentaId(fieldSet.readLong("cuentaId")); } 
                    catch (Exception e) { item.setCuentaId(0L); }
                    
                    item.setNombre(fieldSet.readString("nombre"));
                    
                    String saldoStr = fieldSet.readString("saldo");
                    if (saldoStr != null && !saldoStr.trim().isEmpty()) {
                        try { item.setSaldo(new BigDecimal(saldoStr)); } catch (Exception e) {}
                    }
                    
                    String edadStr = fieldSet.readString("edad");
                    if (edadStr != null && !edadStr.trim().isEmpty()) {
                        try { item.setEdad(Integer.parseInt(edadStr)); } catch (Exception e) {}
                    }
                    
                    item.setTipo(fieldSet.readString("tipo"));
                    return item;
                })
                .build();
    }

    @Bean
    public InteresProcessor interesProcessor() {
        return new InteresProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Interes> interesWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Interes>()
                .dataSource(dataSource)
                .sql("INSERT INTO intereses_procesados (cuenta_id, nombre, saldo, edad, tipo, interes, saldo_final, estado) " +
                    "VALUES (:cuentaId, :nombre, :saldo, :edad, :tipo, :interes, :saldoFinal, :estado)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step interesesMinionStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<Interes> interesReader,
            InteresProcessor interesProcessor,
            JdbcBatchItemWriter<Interes> interesWriter,
            CustomSkipPolicy customSkipPolicy,
            CustomSkipListener customSkipListener) {

        return new StepBuilder("interesesMinionStep", jobRepository)
                .<Interes, Interes>chunk(5, transactionManager) 
                .reader(interesReader)
                .processor(interesProcessor)
                .writer(interesWriter)
                .faultTolerant()
                .skipPolicy(customSkipPolicy)               
                .listener(customSkipListener)               
                .retryLimit(3)                              
                .retry(TransientDataAccessException.class)  
                .build();
    }

    @Bean
    public Step interesesPartitionStep(JobRepository jobRepository, Step interesesMinionStep, TaskExecutor taskExecutor) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(interesesMinionStep);
        partitionHandler.setTaskExecutor(taskExecutor); 
        partitionHandler.setGridSize(3);

        return new StepBuilder("interesesPartitionStep", jobRepository)
                .partitioner("interesesMinionStep", new ArchivoPartitioner(1000)) // <--- 1000
                .partitionHandler(partitionHandler)
                .build();
    }

    @Bean
    public Job interesesJob(JobRepository jobRepository, Step interesesPartitionStep) {
        return new JobBuilder("interesesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(interesesPartitionStep) 
                .build();
    }
}
package com.banco.bank_legacy_batch.partitioner;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import java.util.HashMap;
import java.util.Map;

public class ArchivoPartitioner implements Partitioner {
    private final int totalData;

    // Pasamos el total de líneas del archivo (ej: 10 transacciones)
    public ArchivoPartitioner(int totalData) {
        this.totalData = totalData;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        // Calcula cuántos registros procesará cada hilo
        int partitionSize = (int) Math.ceil((double) totalData / gridSize);
        int start = 0;

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            int end = Math.min(start + partitionSize - 1, totalData - 1);

            context.putInt("start", start);
            context.putInt("end", end);
            context.putInt("partitionSize", partitionSize);
            context.putString("partitionName", "partition" + i);

            partitions.put("partition" + i, context);

            start += partitionSize;
            if (start >= totalData) {
                break;
            }
        }
        return partitions;
    }
}
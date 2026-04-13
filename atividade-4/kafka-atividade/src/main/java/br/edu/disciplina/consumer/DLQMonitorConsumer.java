package br.edu.disciplina.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

public class DLQMonitorConsumer {

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "grupo-dlq-monitor");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of("logfast.dlq"));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                if (records.isEmpty()) continue;

                for (ConsumerRecord<String, String> record : records) {
                    String motivo = "N/A";
                    String topicoOrigem = "N/A";

                    for (Header header : record.headers()) {
                        if (header.key().equals("motivo"))
                            motivo = new String(header.value());
                        if (header.key().equals("topico-origem"))
                            topicoOrigem = new String(header.value());
                    }

                    System.out.printf("=== DLQ REPORT ===%n");
                    System.out.printf("Key: %s%n", record.key());
                    System.out.printf("Motivo: %s%n", motivo);
                    System.out.printf("Tópico origem: %s%n", topicoOrigem);
                    System.out.printf("Timestamp: %s%n", Instant.ofEpochMilli(record.timestamp()));
                    System.out.printf("==================%n");
                }

                consumer.commitSync();
            }
        }
    }
}
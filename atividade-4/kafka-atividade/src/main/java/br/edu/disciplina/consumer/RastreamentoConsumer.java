package br.edu.disciplina.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

public class RastreamentoConsumer {

    private static final String TOPIC = "logfast.eventos.encomenda";
    private static final String GROUP_ID = "grupo-rastreamento";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        Map<String, String> ultimoStatus = new HashMap<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(TOPIC));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                if (records.isEmpty()) continue;

                for (ConsumerRecord<String, String> record : records) {
                    String encomenda = record.key();
                    String valor = record.value();
                    // extrai o status do JSON simples
                    String status = valor.replaceAll(".*\"status\":\"([^\"]+)\".*", "$1");
                    ultimoStatus.put(encomenda, status);
                }

                // imprime estado consolidado do lote
                ultimoStatus.forEach((enc, status) ->
                    System.out.printf("Estado: %s -> %s%n", enc, status));

                consumer.commitSync();
                System.out.println("--- Commit realizado ---");
            }
        }
    }
}
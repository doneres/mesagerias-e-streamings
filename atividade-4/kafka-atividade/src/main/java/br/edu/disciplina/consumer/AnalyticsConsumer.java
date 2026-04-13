package br.edu.disciplina.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class AnalyticsConsumer {

    private static final String TOPIC = "logfast.eventos.encomenda";
    private static final String GROUP_ID = "grupo-analytics-v2";
    private static final String CSV_FILE = "eventos.csv";

    public static void main(String[] args) throws IOException {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
             FileWriter writer = new FileWriter(CSV_FILE, true)) {

            writer.write("encomendaId,status,timestamp\n");
            consumer.subscribe(List.of(TOPIC));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                if (records.isEmpty()) continue;

                for (ConsumerRecord<String, String> record : records) {
                    String encomenda = record.key();
                    String valor = record.value();
                    String status = valor.replaceAll(".*\"status\":\"([^\"]+)\".*", "$1");
                    String timestamp = valor.replaceAll(".*\"timestamp\":\"([^\"]+)\".*", "$1");
                    writer.write(encomenda + "," + status + "," + timestamp + "\n");
                }

                writer.flush(); // flush antes do commit
                consumer.commitSync();
                System.out.println("Lote gravado no CSV e commit realizado.");
            }
        }
    }
}
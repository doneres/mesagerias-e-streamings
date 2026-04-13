package br.edu.disciplina.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class ConsumerComDLQ {

    private static final int MAX_TENTATIVAS = 3;
    private static final String TOPICO_DLQ = "logfast.dlq";

    public static void main(String[] args) {

        Properties propsConsumer = new Properties();
        propsConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, "grupo-dlq-demo");
        propsConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        propsConsumer.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        Properties propsProducer = new Properties();
        propsProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        propsProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(propsConsumer);
             KafkaProducer<String, String> dlqProducer = new KafkaProducer<>(propsProducer)) {

            consumer.subscribe(List.of("logfast.eventos.encomenda"));

            while (true) {
                ConsumerRecords<String, String> lote = consumer.poll(Duration.ofMillis(300));
                for (ConsumerRecord<String, String> record : lote) {
                    processarComRetry(record, dlqProducer);
                }
                if (!lote.isEmpty()) consumer.commitSync();
            }
        }
    }

    private static void processarComRetry(
            ConsumerRecord<String, String> record,
            KafkaProducer<String, String> dlqProducer) {

        int tentativa = 0;
        while (tentativa < MAX_TENTATIVAS) {
            try {
                processarEvento(record);
                return;
            } catch (Exception ex) {
                tentativa++;
                System.err.printf("Tentativa %d/%d falhou para key=%s: %s%n",
                        tentativa, MAX_TENTATIVAS, record.key(), ex.getMessage());
            }
        }

        ProducerRecord<String, String> dlqRecord =
                new ProducerRecord<>(TOPICO_DLQ, record.key(), record.value());
        dlqRecord.headers().add("motivo", "MAX_TENTATIVAS_ATINGIDO".getBytes());
        dlqRecord.headers().add("topico-origem", record.topic().getBytes());
        dlqProducer.send(dlqRecord);
        dlqProducer.flush();
        System.err.printf("Evento key=%s encaminhado para DLQ.%n", record.key());
    }

    private static void processarEvento(ConsumerRecord<String, String> r) {
        if (r.value().contains("\"status\":\"TRIAGEM\""))
            throw new RuntimeException("Status TRIAGEM não suportado neste servico.");
        System.out.println("Processado: " + r.value());
    }
}
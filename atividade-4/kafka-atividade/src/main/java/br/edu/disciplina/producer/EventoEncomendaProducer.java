package br.edu.disciplina.producer;

import br.edu.disciplina.model.EventoEncomenda;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

public class EventoEncomendaProducer {

    private static final String TOPIC = "logfast.eventos.encomenda";
    private static final List<String> ENCOMENDAS = List.of(
        "ENC-001", "ENC-002", "ENC-003", "ENC-004", "ENC-005"
    );
    private static final List<String> ESTAGIOS = List.of(
        "COLETA", "TRIAGEM", "EM_TRANSITO", "SAIU_PARA_ENTREGA", "ENTREGUE"
    );

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (String encomenda : ENCOMENDAS) {
                for (String estagio : ESTAGIOS) {
                    String timestamp = Instant.now().toString();
                    EventoEncomenda evento = new EventoEncomenda(encomenda, estagio, timestamp);
                    String json = evento.toJson();

                    ProducerRecord<String, String> record =
                        new ProducerRecord<>(TOPIC, encomenda, json);

                    producer.send(record, (metadata, exception) -> {
                        if (exception == null) {
                            System.out.printf("✔ [%s] %s → partição %d | offset %d%n",
                                estagio, encomenda,
                                metadata.partition(), metadata.offset());
                        } else {
                            System.err.println("Erro ao enviar: " + exception.getMessage());
                        }
                    });
                }
            }
            producer.flush(); // ← dentro do try, após os loops
        }
        System.out.println("Todos os eventos publicados!");
    }
}
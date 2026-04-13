package dev.doneres.ativdade_3.service;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import dev.doneres.ativdade_3.DTO.NoticiaDTO;

@Service
public class RedacaoProducer {

    private final JmsTemplate jmsTemplate;

    public RedacaoProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publicarNoticia(NoticiaDTO noticia) {
        // Envia o JSON para a fila chamada "queue.noticias.processamento"
        jmsTemplate.convertAndSend("queue.noticias.processamento", noticia);
        System.out.println("[REDAÇÃO] Matéria enviada para processamento assíncrono: " + noticia.getTitulo());
    }
}

package dev.doneres.ativdade_3.Component;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import dev.doneres.ativdade_3.DTO.NoticiaDTO;

@Component
public class ProcessamentoNoticiaWorker {

    @JmsListener(destination = "queue.noticias.processamento")
    public void processar(NoticiaDTO noticia) {
        System.out.println("\n[WORKER] Iniciando tarefas pesadas para a matéria: " + noticia.getIdMateria());

        try {
            // Simulando tarefas demoradas (I/O)
            System.out.println("-> Gerando thumbnails das imagens...");
            Thread.sleep(2000);

            System.out.println("-> Enviando matéria para o Elasticsearch...");
            Thread.sleep(1500);

            if (noticia.isUrgente()) {
                System.out.println("-> 🚨 DISPARANDO PUSH NOTIFICATIONS (Matéria Urgente)!");
            }

            System.out.println("[WORKER] Processamento finalizado com sucesso!\n");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Erro durante o processamento da matéria.");
        }
    }
}

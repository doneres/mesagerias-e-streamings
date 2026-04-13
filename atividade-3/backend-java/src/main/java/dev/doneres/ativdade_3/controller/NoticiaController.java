package dev.doneres.ativdade_3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.doneres.ativdade_3.DTO.NoticiaDTO;
import dev.doneres.ativdade_3.service.RedacaoProducer;

@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    private final RedacaoProducer producer;

    public NoticiaController(RedacaoProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/publicar")
    public ResponseEntity<String> simularPublicacao(@RequestBody NoticiaDTO noticia) {
        producer.publicarNoticia(noticia);
        return ResponseEntity
                .ok("Matéria salva! O processamento das mídias e notificações está rodando em background.");
    }
}

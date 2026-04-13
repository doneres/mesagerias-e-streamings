package br.edu.disciplina.model;

public class EventoEncomenda {
    private String codigoEncomenda;
    private String status;
    private String timestamp;

    public EventoEncomenda(String codigoEncomenda, String status, String timestamp) {
        this.codigoEncomenda = codigoEncomenda;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String toJson() {
        return String.format(
            "{\"codigoEncomenda\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}",
            codigoEncomenda, status, timestamp
        );
    }
}
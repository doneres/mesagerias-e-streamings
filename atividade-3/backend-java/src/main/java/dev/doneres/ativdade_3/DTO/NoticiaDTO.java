package dev.doneres.ativdade_3.DTO;

import java.io.Serializable;

public class NoticiaDTO implements Serializable {
    private String idMateria;
    private String titulo;
    private String autor;
    private boolean urgente;

    // Construtores, Getters e Setters
    public NoticiaDTO(String idMateria, String titulo, String autor, boolean urgente) {
        this.idMateria = idMateria;
        this.titulo = titulo;
        this.autor = autor;
        this.urgente = urgente;
    }

    public String getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(String idMateria) {
        this.idMateria = idMateria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public boolean isUrgente() {
        return urgente;
    }

    public void setUrgente(boolean urgente) {
        this.urgente = urgente;
    }

}

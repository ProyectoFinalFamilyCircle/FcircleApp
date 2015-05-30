package com.proyecto.fcircle.clases;


public class Recado {
    private String titulo;
    private String descripcion;
    private String usuarioCreador;
    private String usuarioReceptor;

    public Recado(){}

    public Recado(String titulo, String descripcion, String usuarioCreador, String usuarioReceptor) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.usuarioCreador = usuarioCreador;
        this.usuarioReceptor = usuarioReceptor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(String usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public String getUsuarioReceptor() {
        return usuarioReceptor;
    }

    public void setUsuarioReceptor(String usuarioReceptor) {
        this.usuarioReceptor = usuarioReceptor;
    }
}

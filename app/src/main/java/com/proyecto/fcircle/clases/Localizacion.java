package com.proyecto.fcircle.clases;

public class Localizacion {
    private int id;
    private double longitud;
    private double latitud;
    private String usuario;

    public Localizacion() {

    }

    public Localizacion(double longitud, double latitud, String usuario) {
        this.longitud = longitud;
        this.latitud = latitud;
        this.usuario = usuario;
    }

    public Localizacion(int id, double longitud, double latitud, String usuario) {
        this.id = id;
        this.longitud = longitud;
        this.latitud = latitud;
        this.usuario = usuario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
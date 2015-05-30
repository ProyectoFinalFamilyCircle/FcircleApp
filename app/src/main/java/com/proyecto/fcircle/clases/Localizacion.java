package com.proyecto.fcircle.clases;


import android.location.Location;

public class Localizacion {
    private Location localizacion;
    private String usuario;

    public Localizacion(){

    }

    public Localizacion(Location localizacion, String usuario) {
        this.localizacion = localizacion;
        this.usuario = usuario;
    }

    public Location getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(Location localizacion) {
        this.localizacion = localizacion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }


}

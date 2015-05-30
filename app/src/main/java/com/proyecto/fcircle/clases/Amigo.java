package com.proyecto.fcircle.clases;

import android.location.Location;

public class Amigo {
    private String usuarioInvita;
    private String usuarioAcepta;

    public Amigo() {
    }

    public Amigo(String usuarioInvita, String usuarioAcepta) {
        this.usuarioAcepta = usuarioAcepta;
        this.usuarioInvita = usuarioInvita;
    }

    public String getUsuarioAcepta() {
        return usuarioAcepta;
    }

    public void setUsuarioAcepta(String usuarioAcepta) {
        this.usuarioAcepta = usuarioAcepta;
    }

    public String getUsuarioInvita() {
        return usuarioInvita;
    }

    public void setUsuarioInvita(String usuarioInvita) {
        this.usuarioInvita = usuarioInvita;
    }
}

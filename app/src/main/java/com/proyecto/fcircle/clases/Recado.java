package com.proyecto.fcircle.clases;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Recado implements Serializable, Parcelable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Recado recado = (Recado) o;

        if (descripcion != null ? !descripcion.equals(recado.descripcion) : recado.descripcion != null)
            return false;
        if (titulo != null ? !titulo.equals(recado.titulo) : recado.titulo != null) return false;
        if (usuarioCreador != null ? !usuarioCreador.equals(recado.usuarioCreador) : recado.usuarioCreador != null)
            return false;
        if (usuarioReceptor != null ? !usuarioReceptor.equals(recado.usuarioReceptor) : recado.usuarioReceptor != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = titulo != null ? titulo.hashCode() : 0;
        result = 31 * result + (descripcion != null ? descripcion.hashCode() : 0);
        result = 31 * result + (usuarioCreador != null ? usuarioCreador.hashCode() : 0);
        result = 31 * result + (usuarioReceptor != null ? usuarioReceptor.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.titulo);
        parcel.writeString(this.descripcion);
        parcel.writeString(this.usuarioCreador);
        parcel.writeString(this.usuarioReceptor);
    }

    public Recado(Parcel p){
        this.titulo = p.readString();
        this.descripcion = p.readString();
        this.usuarioCreador = p.readString();
        this.usuarioReceptor = p.readString();
    }

    public static final Creator<Recado> CREATOR = new Creator<Recado>() {

        @Override
        public Recado createFromParcel(Parcel parcel) {
            String titulo = parcel.readString();
            String descripcion = parcel.readString();
            String usuarioCreador = parcel.readString();
            String usuarioReceptor = parcel.readString();
            return new Recado(titulo,descripcion,usuarioCreador,usuarioReceptor);
        }

        @Override
        public Recado[] newArray(int i) {
            return new Recado[0];
        }
    };
}

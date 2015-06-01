package com.proyecto.fcircle.clases;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Usuario implements Serializable, Parcelable{
    private String nombre;
    private String apellidos;
    private String usuario;
    private String clave;

    public Usuario(){

    }

    public Usuario(String nombre, String apellidos, String usuario, String clave) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.usuario = usuario;
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Usuario usuario1 = (Usuario) o;

        if (nombre != null ? !nombre.equals(usuario1.nombre) : usuario1.nombre != null)
            return false;
        if (apellidos != null ? !apellidos.equals(usuario1.apellidos) : usuario1.apellidos != null)
            return false;
        if (usuario != null ? !usuario.equals(usuario1.usuario) : usuario1.usuario != null)
            return false;
        return !(clave != null ? !clave.equals(usuario1.clave) : usuario1.clave != null);

    }

    @Override
    public int hashCode() {
        int result = nombre != null ? nombre.hashCode() : 0;
        result = 31 * result + (apellidos != null ? apellidos.hashCode() : 0);
        result = 31 * result + (usuario != null ? usuario.hashCode() : 0);
        result = 31 * result + (clave != null ? clave.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.nombre);
        parcel.writeString(this.apellidos);
        parcel.writeString(this.usuario);
        parcel.writeString(this.clave);
    }

    public Usuario(Parcel p){
        this.nombre = p.readString();
        this.apellidos = p.readString();
        this.usuario = p.readString();
        this.clave = p.readString();
    }

    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {

        @Override
        public Usuario createFromParcel(Parcel parcel) {
            String nombre = parcel.readString();
            String apellidos = parcel.readString();
            String usuario = parcel.readString();
            String clave = parcel.readString();
            return new Usuario(nombre,apellidos,usuario,clave);
        }

        @Override
        public Usuario[] newArray(int i) {
            return new Usuario[0];
        }
    };

    @Override
    public String toString() {
        return "Usuario{" +
                "nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", usuario='" + usuario + '\'' +
                ", clave='" + clave + '\'' +
                '}';
    }
}


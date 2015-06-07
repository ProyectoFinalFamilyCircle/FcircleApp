package com.proyecto.fcircle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Principal extends Activity {

    public static String URL = "http://192.168.1.14:8080/FcircleServidor/ControlAndroid?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
    }


    public void login(View v){
        Intent i = new Intent(this, LoginUsuario.class);
        this.finish();
        startActivity(i);
    }

    public void registro(View v){
        Intent i = new Intent(this, RegistroUsuario.class);
        this.finish();
        startActivity(i);
    }
}

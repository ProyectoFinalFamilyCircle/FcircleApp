package com.proyecto.fcircle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.proyecto.fcircle.clases.Usuario;
import com.proyecto.fcircle.gestion.GestionLocalizacion;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Principal extends Activity {

    ObjectContainer bd;
    private ArrayList<Usuario> alUsuario = new ArrayList<Usuario>();
    public static String URL = "http://192.168.1.105:8080/FcircleServidor/ControlAndroid?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");
        leerBD();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");
    }

    @Override
    protected void onPause() {
        super.onPause();
        bd.close();
    }

    private void leerBD(){
        Usuario usuario = new Usuario(null, null, null, null);
        List<Usuario> usuarios = bd.queryByExample(usuario);
        for(Usuario u: usuarios){
            alUsuario.add(new Usuario(u.getNombre(), u.getApellidos(), u.getUsuario(), u.getClave()));
        }
    }

    public void login(View v){
        Intent i = new Intent(this,LoginUsuario.class);
        Bundle b=new Bundle();
        b.putParcelableArrayList("usuarios",alUsuario);
        i.putExtras(b);
        startActivity(i);
    }

    public void registro(View v){
        Intent i = new Intent(this,RegistroUsuario.class);
        Bundle b=new Bundle();
        b.putParcelableArrayList("usuarios",alUsuario);
        i.putExtras(b);
        startActivity(i);
    }

    public void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}

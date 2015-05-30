package com.proyecto.fcircle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.proyecto.fcircle.clases.Usuario;

import java.io.Serializable;
import java.util.ArrayList;


public class LoginUsuario extends Activity {
    ObjectContainer bd;
    private ArrayList<Usuario> alUsuario = new ArrayList<Usuario>();
    private EditText etUsuario, etClave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_login_usuario);
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");

        Bundle b = getIntent().getExtras();
        alUsuario = b.getParcelableArrayList("usuarios");

        etUsuario = (EditText) this.findViewById(R.id.etUsuario);
        etClave = (EditText) this.findViewById(R.id.etClave);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bd.close();
    }

    public void loginUsuario(View v){
        String usuario = etUsuario.getText().toString();
        String clave = etClave.getText().toString();
        Boolean todoCorrecto = false;
        for (int i = 0; i < alUsuario.size() ; i++) {
            if (usuario.equals(alUsuario.get(i).getUsuario()) && clave.equals(alUsuario.get(i).getClave())){
                todoCorrecto = true;
            }
        }
        if(todoCorrecto){
            tostada("Login correcto");
            /*Intent i = new Intent(this,CrearRecado.class);
            Bundle b=new Bundle();
            b.putParcelableArrayList("usuarios", alUsuario);
            i.putExtras(b);
            startActivity(i);*/
            Intent i = new Intent(getApplicationContext(), GoogleMaps.class);
            Bundle b=new Bundle();
            b.putParcelableArrayList("usuarios", alUsuario);
            i.putExtras(b);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("EXIT", true);
            startActivity(i);
            Intent intent = new Intent(this, ServicioSesion.class);
            intent.setAction(ServicioSesion.INICIAR);
            startService(intent);
        }else{
            tostada("El usuario o la clave no son correctas");
        }
    }

    public void cancelarLogin(View v){
        this.finish();
    }

    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}

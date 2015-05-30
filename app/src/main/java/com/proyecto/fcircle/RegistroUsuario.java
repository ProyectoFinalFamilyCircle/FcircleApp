package com.proyecto.fcircle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.proyecto.fcircle.clases.Usuario;

import java.util.ArrayList;


public class RegistroUsuario extends Activity {

    ObjectContainer bd;
    private ArrayList<Usuario> alUsuario = new ArrayList<Usuario>();
    private EditText etNombre, etApellidos, etUsuario, etClave, etClaveRepetida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_registro_usuario);
        //Abrimos la base de datos
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");
        //Obtenemos los usuarios
        Bundle b = getIntent().getExtras();
        alUsuario = b.getParcelableArrayList("usuarios");

        etNombre = (EditText) this.findViewById(R.id.etNombre);
        etApellidos = (EditText) this.findViewById(R.id.etApellidos);
        etUsuario = (EditText) this.findViewById(R.id.etUsuario);
        etClave = (EditText) this.findViewById(R.id.etClave);
        etClaveRepetida = (EditText) this.findViewById(R.id.etClaveRepetida);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bd.close();
    }

    public void registrarUsuario(View v){
        final Usuario usuario = new Usuario();
        try {
            String nombre = etNombre.getText().toString();
            String apellidos = etApellidos.getText().toString();
            String nomUsuario = etUsuario.getText().toString();
            String clave = etClave.getText().toString();
            String claveRepetida = etClaveRepetida.getText().toString();
            if (!nombre.equals("") && !apellidos.equals("") && !nomUsuario.equals("") && !clave.equals("")
                    && !claveRepetida.equals("")) {

                usuario.setNombre(nombre);
                usuario.setApellidos(apellidos);
                Boolean todoCorrecto = true;
                for (int i = 0; i < alUsuario.size(); i++) {
                    if (nomUsuario.equals(alUsuario.get(i).getUsuario())) {
                        tostada("Ya existe el usuario " + nomUsuario);
                        todoCorrecto = false;
                    }
                }
                if (clave.equals(claveRepetida)) {
                    usuario.setClave(clave);
                } else {
                    tostada("Las claves deben coincidir");
                    todoCorrecto = false;
                }
                if (todoCorrecto) {//Si el registro es correcto
                    usuario.setUsuario(nomUsuario);
                    bd.store(usuario);
                    bd.commit();
                    alUsuario.add(usuario);
                    tostada("Registrado correctamente");
                    //Como el registro es correcto, mostramos la actividad login
                    Intent i = new Intent(this, LoginUsuario.class);
                    Bundle b = new Bundle();
                    b.putParcelableArrayList("usuarios", alUsuario);
                    i.putExtras(b);
                    startActivity(i);
                }
            }else{
                tostada("Faltan campos por rellenar");
            }
        }catch (Exception e){
            tostada("No se ha podido registrar");
        }
    }

    public void cancelarRegistro(View v){
        this.finish();
    }

    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}

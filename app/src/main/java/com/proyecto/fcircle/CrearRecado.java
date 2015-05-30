package com.proyecto.fcircle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.proyecto.fcircle.clases.Usuario;

import java.util.ArrayList;


public class CrearRecado extends Activity {
    private ArrayList<Usuario> alUsuario = new ArrayList<Usuario>();
    private ArrayList<String> usuarios = new ArrayList<String>();
    private EditText etTitulo, etDescripcion;
    private Spinner spUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_crear_recado);
        etTitulo = (EditText) this.findViewById(R.id.etTitulo);
        etDescripcion = (EditText) this.findViewById(R.id.etDescripcion);
        spUsuarios = (Spinner) this.findViewById(R.id.spUsuarios);

        Bundle b = getIntent().getExtras();
        alUsuario = b.getParcelableArrayList("usuarios");
        for (int i = 0; i < alUsuario.size() ; i++) {
            usuarios.add(alUsuario.get(i).getUsuario());
        }

        ArrayAdapter<String> adaptadorSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, usuarios); //selected item will look like a spinner set from XML
        adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUsuarios.setAdapter(adaptadorSpinner);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.crear_recado, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.accion_cerrar_sesion) {
            Intent intent = new Intent(this, ServicioSesion.class);
            intent.setAction(ServicioSesion.CERRAR);
            startService(intent);
            Intent i = new Intent(getApplicationContext(), Principal.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("EXIT", true);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

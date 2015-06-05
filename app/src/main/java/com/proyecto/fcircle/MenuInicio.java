package com.proyecto.fcircle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MenuInicio extends Activity {


    private String usuarioLogueado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_menu_inicio);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inicio, menu);
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

    public void agregarRecado(View v){
        Intent i = new Intent(this, CrearRecado.class);
        startActivity(i);
    }

    public void misLocalizaciones(View v){
        Intent i = new Intent(this, GoogleMaps.class);
        startActivity(i);
    }

    public void agregarFamiliar(View v){
        Intent i = new Intent(this, AgregarFamiliar.class);
        startActivity(i);
    }

    public void misRecados(View v){
        Intent i = new Intent(this, MisRecados.class);
        startActivity(i);
    }


    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /***************************** FUNCIONES DEL SERVIDOR ******************************/


}

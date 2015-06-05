package com.proyecto.fcircle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.proyecto.fcircle.clases.Recado;

import java.util.ArrayList;


public class DetalleRecado extends Activity {

    private ArrayList<Recado> alRecado = new ArrayList<Recado>();
    private int posicion;
    private TextView tvTitulo, tvUsuarioCreador, tvDescripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_detalle_recado);
        initComponents();
        mostrarRecado();
    }

    public void initComponents(){
        alRecado = getIntent().getExtras().getParcelableArrayList("recados");
        posicion = getIntent().getExtras().getInt("posicion");
        tvTitulo = (TextView) findViewById(R.id.tvTitulo);
        tvUsuarioCreador = (TextView) findViewById(R.id.tvUsuarioCreador);
        tvDescripcion = (TextView) findViewById(R.id.tvDescripcion);
    }

    public void mostrarRecado(){
        tvTitulo.setText(alRecado.get(posicion).getTitulo());
        tvUsuarioCreador.setText(alRecado.get(posicion).getUsuarioCreador());
        tvDescripcion.setText(alRecado.get(posicion).getDescripcion());
    }
}

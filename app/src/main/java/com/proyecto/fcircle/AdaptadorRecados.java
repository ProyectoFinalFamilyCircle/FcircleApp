package com.proyecto.fcircle;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.proyecto.fcircle.clases.Recado;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorRecados extends ArrayAdapter<Recado> {
    private Context contexto;
    private ArrayList<Recado> lRecado;
    private int recurso;
    static LayoutInflater i;

    public AdaptadorRecados(Context context, int resource, ArrayList<Recado> objects) {
        super(context, resource, objects);
        this.contexto = context;
        this.lRecado = objects;
        this.recurso = resource;
        this.i = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.v("LOG", "" + lRecado.size());
        viewHolder vh = null;
        if (convertView == null){
            convertView = i.inflate(recurso, null);
            vh = new viewHolder();
            vh.tvTitulo = (TextView) convertView.findViewById(R.id.tvTitulo);
            vh.tvUsuario = (TextView) convertView.findViewById(R.id.tvUsuario);
            convertView.setTag(vh);
        }else {
            vh = (viewHolder) convertView.getTag();
        }
        vh.posicion = position;

        vh.tvTitulo.setText(lRecado.get(position).getTitulo());
        vh.tvUsuario.setText(lRecado.get(position).getUsuarioCreador());
        return convertView;
    }

    static class viewHolder{
        public TextView tvTitulo, tvUsuario;
        public int posicion;
    }
}

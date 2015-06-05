package com.proyecto.fcircle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.proyecto.fcircle.clases.Amigo;
import com.proyecto.fcircle.clases.Recado;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MisRecados extends Activity {

    ObjectContainer bd;
    private String usuarioLogueado;
    private ArrayList<Recado> alRecado = new ArrayList<Recado>();
    private ListView lvMisRecados;
    private AdaptadorRecados adaptadorR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_mis_recados);
        //Obtenemos el usuario logueado.
        usuarioLogueado = getUsuarioSharedPreferences();
        lvMisRecados = (ListView) findViewById(R.id.lvMisRecados);
        //Abrimos la base de datos db4o.
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");
        //Lanzamos la hebra para actualizar los recados.
        ActualizarRecados aRecados = new ActualizarRecados();
        aRecados.execute();
        //Una vez actualizados leemos la bd db4o.
        leerBD();
        //Añadimos los recados a la lista.
        adaptadorR = new AdaptadorRecados(this, R.layout.detalle_lista_recado, alRecado);
        lvMisRecados.setAdapter(adaptadorR);
        registerForContextMenu(lvMisRecados);
        adaptadorR.notifyDataSetChanged();

        lvMisRecados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MisRecados.this, DetalleRecado.class);
                intent.putExtra("recados",alRecado);
                intent.putExtra("posicion",i);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        bd.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mis_recados, menu);
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

    private void leerBD(){
        Recado recadoConsulta = new Recado(null, null, null, usuarioLogueado);
        List<Recado> listaRecados = bd.queryByExample(recadoConsulta);
        for (Recado r : listaRecados){
            alRecado.add(new Recado(r.getTitulo(), r.getDescripcion(), r.getUsuarioCreador(), r.getUsuarioReceptor()));
        }
    }

    public String getUsuarioSharedPreferences() {
        SharedPreferences sp = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        return sp.getString("usuario", "");
    }

    /****************************** CONEXION CON LA BD *******************************************/

    class ActualizarRecados extends AsyncTask<String,Integer,String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MisRecados.this);
            pDialog.setMessage("Actualizando recados");
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String url = "target=recado&" + "op=select&" + "action=view";
            String r = leerpagina(Principal.URL + url);
            return r;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            Log.v("AQUI", strings);
            cargarMisRecados(strings);
            pDialog.dismiss();
        }

        public void cargarMisRecados(String s){
            JSONTokener token = new JSONTokener(s);
            try {
                JSONArray array = new JSONArray(token);
                for(int i=0;i<array.length(); i++){
                    JSONObject fila = array.getJSONObject(i);
                    Recado recado = new Recado(fila.getString("titulo"),
                                                fila.getString("descripcion"),
                                                fila.getString("usuarioCreador"),
                                                fila.getString("usuarioReceptor"));
                    //Comprobamos si el usuarioReceptor es el usuario que esta logueado, para mostrar sus recados.
                    if(recado.getUsuarioReceptor().equals(usuarioLogueado)){
                        //añadir en db4o
                        //comprobar que no esta en db4o
                        List<Recado> recados = bd.queryByExample(new Recado(null, null, null, usuarioLogueado));
                        if(recados.size() == 0) {
                            //guardamos cuando no lo tenemos en nuestra lista
                            bd.store(recado);
                            bd.commit();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public String leerpagina(String data){
            URL url;
            InputStream is = null;
            BufferedReader br;
            String line,out="";
            try{
                url = new URL(data);
                is = url.openStream();  // throws an IOException
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    out+=line+"\n";
                }
                br.close();
                is.close();
                return out;
            }catch(IOException e){
                System.out.println(e);
            }
            return "no se ha podido leer";
        }
    }
}

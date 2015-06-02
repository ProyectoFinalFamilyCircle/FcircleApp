package com.proyecto.fcircle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.proyecto.fcircle.clases.Amigo;
import com.proyecto.fcircle.clases.Usuario;

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


public class MenuInicio extends Activity {

    ObjectContainer bd;
    private String usuarioLogueado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_menu_inicio);
        //Abrimos la base de datos db4o
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");

        Bundle b = getIntent().getExtras();
        usuarioLogueado = b.getString("usuarioLogueado");

        Actualizar actualizar = new Actualizar();
        actualizar.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bd.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inicio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void agregarRecado(View v){
        Intent i = new Intent(this, CrearRecado.class);
        Bundle b = new Bundle();
        b.putString("usuarioLogueado", usuarioLogueado);
        i.putExtras(b);
        startActivity(i);
    }

    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /***************************** FUNCIONES DEL SERVIDOR ******************************/

    class Actualizar extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... params) {
            String url = "target=amigo&" + "op=select&" + "action=view";
            String r = leerpagina(Principal.URL + url);
            return r;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            Log.v("AQUI", strings);
            cargarMisAmigos(strings);
        }

        public void cargarMisAmigos(String s){
            JSONTokener token = new JSONTokener(s);
            try {
                JSONArray array = new JSONArray(token);
                for(int i=0;i<array.length(); i++){
                    JSONObject fila = array.getJSONObject(i);
                    Amigo amigo = new Amigo(fila.getString("usuarioInvita"),
                            fila.getString("usuarioAcepta"));

                    //guardamos usuarioAcepta como usuarioLogueado, siempre el primero!!!!!!!!!
                    if(amigo.getUsuarioInvita().equals(usuarioLogueado)){
                        //añadir en db4o
                        //comprobar que no esta en db4o
                        List<Amigo> amigos = bd.queryByExample(new Amigo(usuarioLogueado, amigo.getUsuarioAcepta()));
                        if(amigos.size() == 0) {
                            //guardamos cuando no lo tenemos en nuestra lista
                            bd.store(amigo);
                            bd.commit();
                        }
                    }else if(amigo.getUsuarioAcepta().equals(usuarioLogueado)){
                        //añadir en db4o
                        //comprobar que no esta en db4o
                        List<Amigo> amigos = bd.queryByExample(new Amigo(usuarioLogueado, amigo.getUsuarioInvita()));
                        if(amigos.size() == 0) {
                            //guardamos cuando no lo tenemos en nuestra lista
                            bd.store(new Amigo(usuarioLogueado, amigo.getUsuarioInvita()));
                            bd.commit();
                        }
                    }
                }
            } catch (JSONException e) {
                Log.v("ser","noseer");
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

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
import android.widget.EditText;
import android.widget.Toast;

import com.proyecto.fcircle.clases.Amigo;
import com.proyecto.fcircle.clases.Usuario;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class AgregarFamiliar extends Activity {

    private String usuarioLogueado;
    private String usuarioAgregado;
    private EditText etUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_agregar_familiar);
        initComponents();
    }

    public void initComponents(){
        etUsuario = (EditText) this.findViewById(R.id.etUsuario);
        usuarioLogueado = getUsuarioSharedPreferences();
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

    public void agregar(View v){
        usuarioAgregado = etUsuario.getText().toString();
        ActualizarFamilia actualiza = new ActualizarFamilia();
        actualiza.execute();
    }

    public String getUsuarioSharedPreferences() {
        SharedPreferences sp = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        return sp.getString("usuario", "");
    }

    public void cancelar(View v){
        this.finish();
    }

    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /********************    CONEXION CON EL SERVIDOR ******************************/

    class SubirFamiliar extends AsyncTask<String,Integer,String> {

        Amigo amigo = new Amigo();

        public SubirFamiliar(Amigo ami){
            amigo = ami;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = null;
            String r = null;
            url = Principal.URL + "target=amigo" +
                    "&op=insert" +
                    "&action=op";
            r = post(url);
            return r;
        }

        public String post(String urlPeticion) {
            String resultado="";
            try {
                URL url = new URL(urlPeticion);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setDoOutput(true);
                conexion.setRequestMethod("POST");
                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
                multipartEntity.addPart("usuarioAcepta", new StringBody(amigo.getUsuarioAcepta().toString()));
                multipartEntity.addPart("usuarioInvita", new StringBody(amigo.getUsuarioInvita().toString()));

                conexion.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
                OutputStream out = conexion.getOutputStream();

                try {
                    multipartEntity.writeTo(out);
                } catch (Exception e){
                    Log.v("1.Exception", e.toString());
                    return e.toString();
                }finally {
                    out.close();
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                String decodedString;

                while ((decodedString = in.readLine()) != null) {
                    resultado+=decodedString;
                }
                in.close();
            } catch (MalformedURLException ex) {
                Log.v("2.MalformedURLException",ex.toString());
                return null;
            } catch (IOException ex) {
                Log.v("3.IOException",ex.toString());
                return null;
            }
            return resultado;
        }
    }

    class ActualizarFamilia extends AsyncTask<String,Integer,ArrayList<String>> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AgregarFamiliar.this);
            pDialog.setMessage("Sincronizando datos");
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.show();
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            ArrayList<String> lista = new ArrayList();
            String url = "target=amigo&" + "op=select&" + "action=view";
            String r = leerpagina(Principal.URL + url);
            lista.add(r);
            String url2 = "target=persona&" + "op=select&" + "action=view";
            String r2 = leerpagina(Principal.URL + url2);
            lista.add(r2);
            return lista;
        }

        @Override
        protected void onPostExecute(ArrayList<String> lista) {
            super.onPostExecute(lista);
            if(agregadoExiste(lista.get(1).toString())){
                agregarAmigo(lista.get(0).toString());
            }else{
                tostada(usuarioAgregado + " no existe");
            }
            pDialog.dismiss();
        }

        public void agregarAmigo(String s){

            boolean agregar = true;
            JSONTokener token = new JSONTokener(s);
            try {
                JSONArray array = new JSONArray(token);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject fila = array.getJSONObject(i);
                    String usuarioAcepta = fila.getString("usuarioAcepta");
                    String usuarioInvita = fila.getString("usuarioInvita");
                    Amigo amigoJson = new Amigo(usuarioInvita, usuarioAcepta);

                    /*****************************************************/

                    if (sonIguales(amigoJson)) {
                        agregar = false;
                        break;
                    }
                }
                if(agregar){
                    agregarAmigo();
                }
            } catch (JSONException e) {
                Log.v("JSONException", "leerJSON");
            }

        }

        public void agregarAmigo(){
            //los hacemos amigos
            Amigo amigo = new Amigo(usuarioLogueado, usuarioAgregado);
            SubirFamiliar subir = new SubirFamiliar(amigo);
            subir.execute();
            tostada("Amigo agregado");
        }

        public boolean sonIguales(Amigo amigoJson){
            if(usuarioLogueado.equals(amigoJson.getUsuarioAcepta()) || usuarioLogueado.equals(amigoJson.getUsuarioInvita())){
                if(usuarioAgregado.equals(amigoJson.getUsuarioAcepta()) || usuarioAgregado.equals(amigoJson.getUsuarioInvita())) {
                    //coinciden, ya son amigos
                    tostada( usuarioAgregado + " ya se encuentra en tu lista de amigos.");
                    return true;
                }
            }
            return false;
        }

        public boolean agregadoExiste(String s){
            JSONTokener token = new JSONTokener(s);
            try {
                JSONArray array = new JSONArray(token);
                for(int i=0;i<array.length(); i++){
                    JSONObject fila = array.getJSONObject(i);

                    Usuario usuario = new Usuario(fila.getString("nombre"),
                            fila.getString("apellidos"),
                            fila.getString("nombreUsuario"),
                            fila.getString("clave"));

                    if(usuario.getUsuario().equals(usuarioAgregado)){
                        //romper el bucle
                        //usuario agregado existe
                        return true;
                    }
                }
            } catch (JSONException e) {
                Log.v("JSONException", "comprobarAgregadoExiste");
            }
            return false;
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

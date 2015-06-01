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
import java.util.List;


public class AgregarFamiliar extends Activity {

    private String usuarioLogueado = "jorge";
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.agregar_familiar, menu);
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

        Actualizar actualiza = new Actualizar();
        actualiza.execute();
    }

    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /********************    CONEXION CON EL SERVIDOR ******************************/

    class Subir extends AsyncTask<String,Integer,String> {

        ProgressDialog pDialog;
        Amigo amigo = new Amigo();

        public Subir(Amigo ami){
            amigo = ami;
        }

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
        protected String doInBackground(String... params) {
            String url = null;
            String r = null;
            url = Principal.URL + "target=amigo" +
                    "&op=insert" +
                    "&action=op";
            r = post(url);
            return r;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            tostada(strings);
            pDialog.dismiss();
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
                    Log.v("primero", e.toString());
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
                Log.v("segundo",ex.toString());
                return null;
            } catch (IOException ex) {
                Log.v("tercero",ex.toString());
                return null;
            }
            return resultado;
        }
    }

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
            leerJSON(strings);
        }

        public void leerJSON(String s){
            JSONTokener token = new JSONTokener(s);
            try {
                JSONArray array = new JSONArray(token);
                for(int i=0;i<array.length(); i++){
                    JSONObject fila = array.getJSONObject(i);
                    String usuarioAcepta = fila.getString("usuarioAcepta");
                    String usuarioInvita = fila.getString("usuarioInvita");
                    //Que coincidan con logueado y agregado


                    /*****************************************************/


                    if(usuarioLogueado.equals(usuarioAcepta) || usuarioLogueado.equals(usuarioInvita)){
                        if(usuarioAgregado.equals(usuarioAcepta) || usuarioAgregado.equals(usuarioInvita)) {
                            //coinciden
                            //tostada diciendo que ya se conocen
                            tostada( usuarioAgregado + " ya se encuentra en tu lista de amigos.");
                        }else{
                            //no coinciden
                            Amigo amigo = new Amigo(usuarioLogueado, usuarioAgregado);
                            Subir subir = new Subir(amigo);
                            subir.execute();
                            tostada("Amigo agregado");
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

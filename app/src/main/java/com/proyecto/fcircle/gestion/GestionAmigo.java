package com.proyecto.fcircle.gestion;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
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
import java.util.List;


public class GestionAmigo {

    String URL="http://192.168.1.13:8080/FcircleServidor/ControlAndroid?";

    public void subir(Context c){
        Subir subir = new Subir(c);
        subir.execute();
    }

    class Subir extends AsyncTask<String,Integer,String> {

        ProgressDialog pDialog;

        private Context context;

        public Subir(Context contexto){
            context=contexto;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Sincronizando datos");
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            String url = null;
            String r = null;
            url = URL + "target=amigo" +
                    "&op=insert" +
                    "&action=op";
            r = post(url);
            return r;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            Toast.makeText(context, strings, Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }

        /**
         * *************** Envia los datos al servidor *********************************
         */
        /*
        public String subirDatos(String data) {
            URL url;
            InputStream is = null;
            BufferedReader br;
            try {
                url = new URL(data);
                is = url.openStream();

                br = new BufferedReader(new InputStreamReader(is));
                br.close();
                is.close();
                return "sincronizacion finalizada";
            } catch (IOException e) {
                System.out.println(e);
            }
            return "error de sincronizacion";
        }
*/
        public String post(String urlPeticion) {
            String resultado="";
            try {
                URL url = new URL(urlPeticion);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setDoOutput(true);
                conexion.setRequestMethod("POST");
                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);

                multipartEntity.addPart("usuarioAcepta", new StringBody("cabezon"));
                multipartEntity.addPart("usuarioInvita", new StringBody("es"));

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

        private Context contexto;
        ObjectContainer bd;

        public Actualizar(Context contex){
            contexto = contex;
        }

        @Override
        protected String doInBackground(String... params) {
            bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), contexto.getExternalFilesDir(null) + "/bd.db4o");
            String url = "?target=amigo&" + "op=select&" + "action=view";
            String r = leerpagina(URL + url);
            return r;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            leerJSON(strings);
            tostada(strings);
        }

        public void leerJSON(String s){
            JSONTokener token = new JSONTokener(s);
            try {
                JSONArray array = new JSONArray(token);
                for(int i=0;i<array.length(); i++){
                    JSONObject fila = array.getJSONObject(i);

                    boolean distinto=true;
                    //hay que recorrer la base de datos para que no se repitan los objetos

                    /*****************/
                    Amigo amigo = new Amigo(fila.getString("usuarioAcepta"), fila.getString("usuarioInvita"));
                    List<Amigo> amigos = bd.queryByExample(amigo);
                    if(amigos.size()==0) {
                        //guardamos cuando no lo tenemos en nuestra lista
                        bd.store(amigo);
                        bd.commit();
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

        public void tostada(String strings){
            Toast.makeText(contexto, strings, Toast.LENGTH_SHORT).show();
        }
    }
}


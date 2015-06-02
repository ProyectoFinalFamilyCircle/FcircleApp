package com.proyecto.fcircle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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


public class RegistroUsuario extends Activity {

    ObjectContainer bd;
    private ArrayList<Usuario> alUsuario = new ArrayList<Usuario>();
    private EditText etNombre, etApellidos, etUsuario, etClave, etClaveRepetida;

    private Usuario usuarioRegistrado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_registro_usuario);
        //Abrimos la base de datos db4o
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");
        //Leemos los datos del servidor

        Actualizar actualiza = new Actualizar();
        actualiza.execute();
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

                    // para no tener que descargar todos los usuarios
                    //usuarioRegistrado=usuario;

                    bd.store(usuario);
                    bd.commit();
                    alUsuario.add(usuario);
                    tostada("Registrado correctamente");
                    //llamamos a la hebra
                    Subir subir = new Subir(usuario);
                    subir.execute();
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

    /************************* CONEXION CON EL SERVIDOR ****************************************/

    class Subir extends AsyncTask<String,Integer,String> {

        ProgressDialog pDialog;
        Usuario usuario = new Usuario();

        public Subir(Usuario usu){
            usuario = usu;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = null;
            String r = null;
            url = Principal.URL + "target=persona" +
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
                multipartEntity.addPart("nombre", new StringBody(usuario.getNombre().toString()));
                multipartEntity.addPart("apellidos", new StringBody(usuario.getApellidos().toString()));
                multipartEntity.addPart("nombreUsuario", new StringBody(usuario.getUsuario().toString()));
                multipartEntity.addPart("clave", new StringBody(usuario.getClave().toString()));
                multipartEntity.addPart("foto", new StringBody(""));
                multipartEntity.addPart("localizacion", new StringBody(""));

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
            String url = "target=persona&" + "op=select&" + "action=view";
            String r = leerpagina(Principal.URL + url);
            return r;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            tostada(strings);
            Log.v("AQUI", strings);
            leerJSON(strings);
        }

        public void leerJSON(String s){
            JSONTokener token = new JSONTokener(s);
            try {
                JSONArray array = new JSONArray(token);
                for(int i=0;i<array.length(); i++){
                    JSONObject fila = array.getJSONObject(i);
                    //hay que recorrer la base de datos para que no se repitan los objetos
                    /*****************/
                    Usuario usuario = new Usuario(fila.getString("nombre"),
                                                    fila.getString("apellidos"),
                                                    fila.getString("nombreUsuario"),
                                                    fila.getString("clave"));
                    List<Usuario> usuarios = bd.queryByExample(new Usuario(null, null, fila.getString("nombreUsuario"), null));
                    if(usuarios.size() == 0) {
                        //guardamos cuando no lo tenemos en nuestra lista
                        bd.store(usuario);
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
    }

    /*******************************************************************************************/
}

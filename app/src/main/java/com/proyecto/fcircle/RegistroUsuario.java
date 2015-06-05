package com.proyecto.fcircle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

public class RegistroUsuario extends Activity {

    private EditText etNombre, etApellidos, etUsuario, etClave, etClaveRepetida;
    private Usuario usuarioRegistrado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_registro_usuario);
        initComponents();
    }

    public void initComponents(){
        usuarioRegistrado = new Usuario();
        etNombre = (EditText) this.findViewById(R.id.etNombre);
        etApellidos = (EditText) this.findViewById(R.id.etApellidos);
        etUsuario = (EditText) this.findViewById(R.id.etUsuario);
        etClave = (EditText) this.findViewById(R.id.etClave);
        etClaveRepetida = (EditText) this.findViewById(R.id.etClaveRepetida);
    }

    public void registrarUsuario(View v){
        Boolean todoCorrecto = true;
        String nombre = etNombre.getText().toString();
        String apellidos = etApellidos.getText().toString();
        String nomUsuario = etUsuario.getText().toString();
        String clave = etClave.getText().toString();
        String claveRepetida = etClaveRepetida.getText().toString();
        //comprobamos que los campos no esten vacios
        if (!nombre.equals("") && !apellidos.equals("") &&
                !nomUsuario.equals("") && !clave.equals("")
                && !claveRepetida.equals("")) {
            //comprobamos que la clave y repetir clave sean iguales
            if (!clave.equals(claveRepetida)) {
                tostada("Las claves deben coincidir");
                todoCorrecto = false;
            }
            if (todoCorrecto) {//Si el registro es correcto
                usuarioRegistrado.setNombre(nombre);
                usuarioRegistrado.setApellidos(apellidos);
                usuarioRegistrado.setClave(clave);
                usuarioRegistrado.setUsuario(nomUsuario);

                //procedemos a comprobar con la base de datos
                ActualizaUsuario actualizaUsuario = new ActualizaUsuario();
                actualizaUsuario.execute();
            }
        }else{
            tostada("Faltan campos por rellenar");
        }
    }

    public void cancelarRegistro(View v){
        Intent i = new Intent(this, Principal.class);
        startActivity(i);
        this.finish();
    }

    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /************************* CONEXION CON EL SERVIDOR ****************************************/

    class SubirUsuario extends AsyncTask<String,Integer,String> {

        Usuario usuario = new Usuario();

        public SubirUsuario(Usuario usu){
            usuario = usu;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = Principal.URL + "target=persona" +
                    "&op=insert" +
                    "&action=op";
            String r = post(url);
            return r;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            //Log.v("SubirUsuario", strings);
            tostada("Registrado correctamente");
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


    class ActualizaUsuario extends AsyncTask<String,Integer,String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(RegistroUsuario.this);
            pDialog.setMessage("Registrando");
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String url = "target=persona&" + "op=select&" + "action=view";
            String r = leerpagina(Principal.URL + url);
            return r;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            //Log.v("ActualizaUsuario", strings);
            //Comprobamos que el usuario no existe
            compruebaUsuario(strings);
            pDialog.dismiss();
            RegistroUsuario.this.finish();
            //Como el registro es correcto, mostramos la actividad login
            Intent i = new Intent(RegistroUsuario.this, LoginUsuario.class);
            startActivity(i);
        }

        public void compruebaUsuario(String s){
            JSONTokener token = new JSONTokener(s);
            boolean registrar = true;
            try {
                JSONArray array = new JSONArray(token);
                for(int i=0;i<array.length(); i++){
                    JSONObject fila = array.getJSONObject(i);
                    /*****************/
                    Usuario usuario = new Usuario(fila.getString("nombre"),
                                                    fila.getString("apellidos"),
                                                    fila.getString("nombreUsuario"),
                                                    fila.getString("clave"));
                    if(usuario.compruebaUsuario(usuarioRegistrado)){
                        tostada(usuarioRegistrado.getUsuario() + " ya está registrado");
                        registrar = false;
                        break;
                    }
                }
                if(registrar){
                    //este usuario no existe en la base de datos, llamamos a la hebra SubirUsuario
                    SubirUsuario subirUsuario = new SubirUsuario(usuarioRegistrado);
                    subirUsuario.execute();
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

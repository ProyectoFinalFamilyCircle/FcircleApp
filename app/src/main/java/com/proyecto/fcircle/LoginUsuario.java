package com.proyecto.fcircle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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


public class LoginUsuario extends Activity {

    private EditText etUsuario, etClave;
    private Usuario usuarioLogueado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_login_usuario);
        initComponents();
    }

    public void initComponents(){
        usuarioLogueado = new Usuario();
        etUsuario = (EditText) this.findViewById(R.id.etUsuario);
        etClave = (EditText) this.findViewById(R.id.etClave);
    }

    public void loginUsuario(View v){
        String usuario = etUsuario.getText().toString();
        String clave = etClave.getText().toString();
        if(!usuario.equals("") && !clave.equals("")){
            usuarioLogueado.setUsuario(usuario);
            usuarioLogueado.setClave(clave);

            ActualizaUsuario actualizaUsuario = new ActualizaUsuario();
            actualizaUsuario.execute();
        }else{
            tostada("Faltan campos por rellenar");
        }
    }

    public void cancelarLogin(View v){
        Intent i = new Intent(this, Principal.class);
        startActivity(i);
        this.finish();
    }

    private void setUsuarioSharedPreferences(String usuario) {
        SharedPreferences sp = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("usuario", usuario);
        ed.apply();
    }

    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /************************* CONEXION CON EL SERVIDOR ****************************************/

    class ActualizaUsuario extends AsyncTask<String,Integer,String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(LoginUsuario.this);
            pDialog.setMessage("Logueando");
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
        }

        public void compruebaUsuario(String s){
            JSONTokener token = new JSONTokener(s);
            boolean loguea = false;
            boolean comunicaError = false;
            try {
                JSONArray array = new JSONArray(token);
                for(int i=0;i<array.length(); i++){
                    JSONObject fila = array.getJSONObject(i);
                    /*****************/
                    Usuario usuario = new Usuario(fila.getString("nombre"),
                            fila.getString("apellidos"),
                            fila.getString("nombreUsuario"),
                            fila.getString("clave"));
                    if(usuario.compruebaUsuario(usuarioLogueado)){
                        if(usuario.compruebaClave(usuarioLogueado)){
                            loguea = true;
                        }else{
                            loguea = false;
                            comunicaError = true;
                            tostada("Contraseña incorrecta");
                        }
                        break;
                    }
                }
                if(loguea){
                    //el logueo es correcto
                    //Guardamos el usuario en el sharedPreferences
                    setUsuarioSharedPreferences(usuarioLogueado.getUsuario());
                    tostada("Login correcto");
                    Intent i = new Intent(getApplicationContext(), GoogleMaps.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("EXIT", true);
                    LoginUsuario.this.finish();
                    startActivity(i);
                    Intent intent = new Intent(LoginUsuario.this, ServicioSesion.class);
                    intent.setAction(ServicioSesion.INICIAR);
                    startService(intent);
                }else if(!comunicaError){
                    tostada("Usuario incorrecto");
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

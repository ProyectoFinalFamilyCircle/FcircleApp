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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.proyecto.fcircle.clases.Amigo;
import com.proyecto.fcircle.clases.Recado;
import com.proyecto.fcircle.clases.Usuario;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class CrearRecado extends Activity {
    ObjectContainer bd;
    private ArrayList<Amigo> alAmigo = new ArrayList<Amigo>();
    private ArrayList<String> amigos = new ArrayList<String>();
    private EditText etTitulo, etDescripcion;
    private Spinner spUsuarios;
    private String usuarioLogueado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_crear_recado);
        initComponent();

        Bundle b = getIntent().getExtras();
        usuarioLogueado = b.getString("usuarioLogueado");
        for (int i = 0; i < alAmigo.size() ; i++) {
            amigos.add(alAmigo.get(i).getUsuarioAcepta());
        }

        ArrayAdapter<String> adaptadorSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, amigos); //selected item will look like a spinner set from XML
        adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUsuarios.setAdapter(adaptadorSpinner);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bd.close();
    }

    public void initComponent(){
        etTitulo = (EditText) this.findViewById(R.id.etTitulo);
        etDescripcion = (EditText) this.findViewById(R.id.etDescripcion);
        spUsuarios = (Spinner) this.findViewById(R.id.spUsuarios);
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");
        leerBD();
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

    public void guardarRecado(View v){
        String titulo = etTitulo.getText().toString();
        String desc = etDescripcion.getText().toString();
        String usuarioReceptor = String.valueOf(spUsuarios.getSelectedItem());
        if(!titulo.equals("") || !desc.equals("") || !usuarioReceptor.equals("")){
            Recado recado = new Recado();
            recado.setTitulo(titulo);
            recado.setDescripcion(desc);
            recado.setUsuarioCreador(usuarioLogueado);
            recado.setUsuarioReceptor(usuarioReceptor);
            //Llamamos a la hebra que almacena el recado en el servidor
            SubirRecado sRecado = new SubirRecado(recado);
            sRecado.execute();
        }else{
            tostada("Hay campos vacíos");
        }
    }

    public void cancelarRecado(View v){
        this.finish();
    }

    private void leerBD(){
        Amigo amigo = new Amigo(usuarioLogueado, null);
        List<Amigo> listaAmigos = bd.queryByExample(amigo);
        for(Amigo a: listaAmigos){
            alAmigo.add(new Amigo(a.getUsuarioInvita(), a.getUsuarioAcepta()));
        }
    }

    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /****************************** CONEXION CON LA BD ****************************************/
    class SubirRecado extends AsyncTask<String,Integer,String> {

        ProgressDialog pDialog;
        Recado recado = new Recado();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(CrearRecado.this);
            pDialog.setMessage("Enviando recado");
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.show();
        }

        public SubirRecado(Recado reca){
            recado = reca;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = null;
            String r = null;
            url = Principal.URL + "target=recado" +
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
                multipartEntity.addPart("titulo", new StringBody(recado.getTitulo().toString()));
                multipartEntity.addPart("descripcion", new StringBody(recado.getDescripcion().toString()));
                multipartEntity.addPart("usuarioCreador", new StringBody(recado.getUsuarioCreador().toString()));
                multipartEntity.addPart("usuarioReceptor", new StringBody(recado.getUsuarioReceptor().toString()));

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
}

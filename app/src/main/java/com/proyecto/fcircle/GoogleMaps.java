package com.proyecto.fcircle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.proyecto.fcircle.clases.Amigo;
import com.proyecto.fcircle.clases.Localizacion;

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

public class GoogleMaps extends Activity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient cliente;
    private LocationRequest peticionLocalizaciones;

    private String usuarioLogueado;
    private ObjectContainer bd;

    private ArrayList<Localizacion> listaLocalizacion;
    private ArrayList<Amigo> listaAmigos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_google_maps);
        initComponents();
        if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            cliente = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            cliente.connect();
        }else{
            tostada("Error de conexión");
        }
    }

    public void initComponents() {
        usuarioLogueado = getUsuarioSharedPreferences();
        //Abrimos la base de datos db4o
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");

        listaLocalizacion = new ArrayList<Localizacion>();
        listaAmigos = new ArrayList<Amigo>();

        ActualizaLocalizacionYAmigos actualizar = new ActualizaLocalizacionYAmigos();
        actualizar.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getMenuInflater().inflate(R.menu.google_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.accion_cerrar_sesion) {
            Intent intent = new Intent(this, ServicioSesion.class);
            intent.setAction(ServicioSesion.CERRAR);
            startService(intent);
            GoogleMaps.this.finish();
            Intent i = new Intent(getApplicationContext(), Principal.class);
            startActivity(i);
            return true;
        }else if (id == R.id.anadir_amigo) {
            Intent i = new Intent(this, AgregarFamiliar.class);
            startActivity(i);
        }else if (id == R.id.mis_recados){
            Intent i = new Intent(this, MisRecados.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        peticionLocalizaciones = new LocationRequest();
        peticionLocalizaciones.setInterval(1000000);
        //peticionLocalizaciones.setFastestInterval(500);
        peticionLocalizaciones.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);//La precision del localizador
        LocationServices.FusedLocationApi.requestLocationUpdates(cliente,
                peticionLocalizaciones, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        cliente.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        cliente.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        ServicioIntent.startAccionGeocode(this, location);
        location.getLatitude();
        location.getLongitude();
        location.bearingTo(location);
        //Toast.makeText(this, location.getLatitude() + ", " + location.getLongitude() + "", Toast.LENGTH_LONG).show();
        Localizacion nuevaLocalizacion = new Localizacion(location.getLongitude(), location.getLatitude(), usuarioLogueado);
        SubirLocalizacion modifica = new SubirLocalizacion(nuevaLocalizacion);
        modifica.execute();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        for (int i = 0; i < listaLocalizacion.size(); i++) {
            if(listaLocalizacion.get(i).getUsuario().equals(usuarioLogueado)){
                LatLng casa = new LatLng(listaLocalizacion.get(i).getLongitud(), listaLocalizacion.get(i).getLatitud());
                map.setMyLocationEnabled(true);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(casa, 13));
                map.addMarker(new MarkerOptions()
                        .title(listaLocalizacion.get(i).getUsuario())
                        .position(casa));
            }else{
                LatLng casa = new LatLng(listaLocalizacion.get(i).getLongitud(), listaLocalizacion.get(i).getLatitud());
                map.addMarker(new MarkerOptions()
                        .title(listaLocalizacion.get(i).getUsuario())
                        .position(casa));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bd.close();
    }

    public String getUsuarioSharedPreferences() {
        SharedPreferences sp = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        return sp.getString("usuario", "");
    }

    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    /************************* CONEXION CON EL SERVIDOR ****************************************/

    class ActualizaLocalizacionYAmigos extends AsyncTask<String,Integer,String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            String url = "target=localizacion&" + "op=select&" + "action=view";
            String[] r = new String[2];
            r[0] = leerpagina(Principal.URL + url);
            url = "target=amigo&" + "op=select&" + "action=view";
            r[1] = leerpagina(Principal.URL + url);
            return r;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            Log.v("ActualizaLocalizacion", strings[0]);
            Log.v("ActualizaAmigos", strings[1]);
            //descargamos a los amigos, para tenerlos en la base de datos
            cargarMisAmigos(strings[1]);

            //descargar las localizaciones de tus amigos
            descargaLocalizaciones(strings[0]);
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
                Log.v("cargarMisAmigos","JSONException");
            }
        }

        public void descargaLocalizaciones(String s){
            JSONTokener token = new JSONTokener(s);
            try {
                JSONArray array = new JSONArray(token);
                for(int i=0;i<array.length(); i++) {
                    JSONObject fila = array.getJSONObject(i);
                    int id = fila.getInt("id");
                    String usuario = fila.getString("usuario");
                    String localizacion = fila.getString("localizacion");
                    String[] arrayLocalizacion = localizacion.split("/");
                    double longitud = Double.parseDouble(arrayLocalizacion[0]);
                    double latitud = Double.parseDouble(arrayLocalizacion[1]);
                    obtenerAmigos();

                    //si el usuario es usuario logueado tambien
                    if(esAmigo(usuario) || usuario.equals(usuarioLogueado)){
                        //ultimas localizaciones
                        int posicion = posicionamiento(usuario);
                        if(posicion>=0){
                            //guardamos al usuario con la nueva localizacion
                            Localizacion l = new Localizacion(longitud, latitud, usuario);
                            listaLocalizacion.remove(posicion);
                            //es indiferente en la posicion en la que guardemos
                            listaLocalizacion.add(posicion, l);
                        }else{
                            //no lo tenemos, lo añadimos nuevo
                            listaLocalizacion.add(new Localizacion(
                                    longitud,
                                    latitud,
                                    usuario
                            ));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public int posicionamiento(String usuario){
            //devolvemos la posicion en el arraylist de localizaciones
            for(Localizacion loc :listaLocalizacion){
                if(loc.getUsuario().equals(usuario)){
                    return listaLocalizacion.indexOf(loc);
                }
            }
            return -1;
        }

        public void obtenerAmigos(){
            Amigo amigoConsulta = new Amigo(usuarioLogueado, null);
            List<Amigo> lAmigos = bd.queryByExample(amigoConsulta);
            for(Amigo a: lAmigos){
                listaAmigos.add(new Amigo(a.getUsuarioInvita(), a.getUsuarioAcepta()));
            }
        }

        public boolean esAmigo(String usuario){
            for (Amigo ami : listaAmigos){
                if(ami.getUsuarioAcepta().equals(usuario))
                    return true;
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

    class SubirLocalizacion extends AsyncTask<String,Integer,String> {

        Localizacion miLocalizacion = new Localizacion();

        public SubirLocalizacion(Localizacion loc){
            miLocalizacion = loc;
        }

        @Override
        protected String doInBackground(String... params) {
            /*
            String delete = post(Principal.URL + "target=localizacion" +
                    "&op=delete" +
                    "&action=op");
            tostada(delete);*/
            String url = Principal.URL + "target=localizacion" +
                    "&op=insert" +
                    "&action=op";
            String r = post(url);
            return r;
        }

        public String post(String urlPeticion) {
            String resultado = "";
            String localizacion = miLocalizacion.getLatitud() + "/" + miLocalizacion.getLongitud();
            try {
                URL url = new URL(urlPeticion);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setDoOutput(true);
                conexion.setRequestMethod("POST");
                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
                multipartEntity.addPart("usuario", new StringBody(miLocalizacion.getUsuario().toString()));
                multipartEntity.addPart("localizacion", new StringBody(localizacion));

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
}


package com.proyecto.fcircle.gestion;

        import android.app.ProgressDialog;
        import android.content.Context;
        import android.os.AsyncTask;
        import android.util.Log;
        import android.widget.Toast;

        import com.proyecto.fcircle.Principal;

        import org.apache.http.entity.mime.HttpMultipartMode;
        import org.apache.http.entity.mime.MultipartEntity;
        import org.apache.http.entity.mime.content.StringBody;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;


public class GestionUsuario {

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
            url = URL + "target=usuario" +
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
         *//*
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
        }*/

        public String post(String urlPeticion) {
            String resultado="";
            try {
                URL url = new URL(urlPeticion);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setDoOutput(true);
                conexion.setRequestMethod("POST");
                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);

                multipartEntity.addPart("nombre", new StringBody("jiorgio"));
                multipartEntity.addPart("apellidos", new StringBody("mariquita"));
                multipartEntity.addPart("nombreUsuario", new StringBody("auler"));
                multipartEntity.addPart("clave", new StringBody("123"));
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
}

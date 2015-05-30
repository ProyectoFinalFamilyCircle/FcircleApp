package com.proyecto.fcircle;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class ServicioIntent extends IntentService {

    private static final String ACCION_GECODE = "com.example.area51.localizacion.action.GEOCODE";
    private static final String EXTRA_PARAM1_LOC = "com.example.area51.localizacion.extra.LOCATION";


    public static void startAccionGeocode(Context context, Location location) {
        Intent intent = new Intent(context, ServicioIntent.class);
        intent.setAction(ACCION_GECODE);
        intent.putExtra(EXTRA_PARAM1_LOC, location);
        context.startService(intent);
    }

    public ServicioIntent() {
        super("ServicioIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {//Este metodo es ejecutado en una hebra automaticamente.
        if (intent != null) {
            final String action = intent.getAction();
            Bundle b = intent.getExtras();
            Location location = b.getParcelable(EXTRA_PARAM1_LOC);
            if(location != null){
                handleAccionGeodecode(location);
            }
        }
    }

    private void handleAccionGeodecode(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try{
            List<Address> direcciones = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (direcciones.size() > 0) {
                Address direccion = direcciones.get(0);
                direccion.getAddressLine(0);
                direccion.getLocality();
                direccion.getCountryName();
                Log.v("Direccion: ",direccion.getAddressLine(0));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

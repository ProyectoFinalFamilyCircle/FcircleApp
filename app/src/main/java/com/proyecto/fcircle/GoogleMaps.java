package com.proyecto.fcircle;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

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
import com.proyecto.fcircle.clases.Localizacion;

public class GoogleMaps extends Activity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient cliente;
    private Location ultimaLocalizacion;
    private LocationRequest peticionLocalizaciones;
    private Localizacion posicionUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_google_maps);
        posicionUsuario = new Localizacion();
        if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            cliente = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            cliente.connect();
        }else{

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        peticionLocalizaciones = new LocationRequest();
        peticionLocalizaciones.setInterval(10000);
        peticionLocalizaciones.setFastestInterval(10000);
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
        posicionUsuario.setLocalizacion(location);
        posicionUsuario.setUsuario("Jorge");
        Toast.makeText(this, posicionUsuario.getLocalizacion().getLatitude() + ", " + posicionUsuario.getLocalizacion().getLongitude() + "", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng casa = new LatLng(37.156457, -3.675781);
        LatLng casa2 = new LatLng(32.256457, -3.675781);
        //LatLng casa = new LatLng(posicionUsuario.getLocalizacion().getLatitude(),posicionUsuario.getLocalizacion().getLongitude());

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(casa, 13));

        map.addMarker(new MarkerOptions()
                .title("Mi casa")
                .snippet("La mejor casa de cúllar vega.")
                .position(casa));


        map.addMarker(new MarkerOptions()
                .title("CASA ")
                .position(casa2));
    }
}

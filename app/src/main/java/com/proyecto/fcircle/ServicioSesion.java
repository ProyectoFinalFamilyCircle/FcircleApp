package com.proyecto.fcircle;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServicioSesion extends Service {
    public static final String INICIAR = "iniciarServicio";
    public static final String CERRAR = "cerrarSesion";

    /* ******************************************************* */
    // METODOS SOBREESCRITOS //
    /* ****************************************************** */

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(INICIAR)){
            notificacion();
        } else if (action.equals(CERRAR)){
            stopForeground(true);
            stopService(new Intent(this, ServicioSesion.class));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void notificacion(){
        Notification note = new Notification(R.drawable.ic_launcher,
                "Family Circle",
                System.currentTimeMillis());
        Intent i=new Intent(this, CrearRecado.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi=PendingIntent.getActivity(this, 0,
                i, 0);

        note.setLatestEventInfo(this, "Family Circle",
                "Red social familiar",
                pi);
        note.flags|=Notification.FLAG_NO_CLEAR;

        startForeground(1337, note);
    }
}

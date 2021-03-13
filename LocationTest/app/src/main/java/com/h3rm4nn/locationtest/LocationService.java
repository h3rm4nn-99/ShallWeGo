package com.h3rm4nn.locationtest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class LocationService extends Service {

    private LocationRequest request;
    private FusedLocationProviderClient locationClient;
    private LocationCallback callbackFunc;

    public LocationService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        request = LocationRequest.create();
        request.setInterval(5000);
        request.setFastestInterval(5000);
        request.setPriority(PRIORITY_HIGH_ACCURACY);
        callbackFunc = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(getApplicationContext(), "Errore", Toast.LENGTH_SHORT).show();
                    return;
                }

                Location l = locationResult.getLastLocation();
                RequestQueue q = Volley.newRequestQueue(getApplicationContext());
                StringRequest request = new StringRequest(Request.Method.PUT, "http://192.168.1.6:8080/api/putLocation?latitude=" + l.getLatitude() + "&longitude=" + l.getLongitude(), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "" + response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                    }
                });
                q.add(request);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String notificationChannelId = "LocationChannel";
        String notificationChannelName = "Canale di Notifica";
        NotificationChannel channel = new NotificationChannel(notificationChannelId, notificationChannelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setLightColor(Color.BLUE);
        channel.enableLights(true);
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        Notification notification = builder.setOngoing(true).setContentTitle("Il servizio di aggiornamento della posizione è in esecuzione").setPriority(NotificationManager.IMPORTANCE_HIGH).setCategory(Notification.CATEGORY_SERVICE).setSmallIcon(R.drawable.ic_near_me_24px).build();
        startForeground(2, notification);
        Toast.makeText(this, "Il servizio è partito!", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new IllegalStateException("Manca il permesso!"); //non dovrebbe comunque succedere in quanto il permesso viene concesso all'avvio dell'app in onCreate() nella MAinActivity
        }
        locationClient.requestLocationUpdates(request, callbackFunc, Looper.getMainLooper());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Il servizio sta per essere distrutto!", Toast.LENGTH_SHORT).show();
        locationClient.removeLocationUpdates(callbackFunc);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
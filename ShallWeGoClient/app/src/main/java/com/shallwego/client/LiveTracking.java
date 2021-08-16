package com.shallwego.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.*;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class LiveTracking extends Service {

    private LocationRequest fusedLocationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback callbackMethod;

    private int rideId;
    private final String user = MainActivity.userName;

    public LiveTracking() {}

    @Override
    public void onCreate() {
        super.onCreate();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationRequest = LocationRequest.create();

        fusedLocationRequest.setInterval(20000);
        fusedLocationRequest.setFastestInterval(20000);
        fusedLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callbackMethod = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Intent messenger = new Intent(LiveTracking.this, MainActivity.class);
                messenger.setAction("freshLocation");
                messenger.putExtra("latitude", locationResult.getLastLocation().getLatitude());
                messenger.putExtra("longitude", locationResult.getLastLocation().getLatitude());
                sendBroadcast(messenger);

                JsonObject object = new JsonObject();
                object.addProperty("latitude", locationResult.getLastLocation().getLatitude());
                object.addProperty("longitude", locationResult.getLastLocation().getLongitude());
                String requestBody = object.toString();
                RequestQueue queue = Volley.newRequestQueue(LiveTracking.this);
                StringRequest request = new StringRequest(Request.Method.PUT, IpAddress.SERVER_IP_ADDRESS + "/api/updateRideLocation/" + rideId + "/" + MainActivity.userName, (response) -> {

                }, Throwable::printStackTrace) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
                    }
                };

                RetryPolicy mRetryPolicy = new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                request.setRetryPolicy(mRetryPolicy);
                queue.add(request);
            }
        };
    }

    @SuppressLint("MissingPermission") // got on first run
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        switch (intent.getAction()) {
            case "STOP_SERVICE": {
                fusedLocationClient.removeLocationUpdates(callbackMethod);
                RequestQueue queue = Volley.newRequestQueue(LiveTracking.this);
                StringRequest request = new StringRequest(Request.Method.PUT, IpAddress.SERVER_IP_ADDRESS + "/api/terminateRide/" + rideId, (response) -> {
                    Intent stopMessenger = new Intent(this, MainActivity.class); // again, can't come up with a better name ;);
                    stopMessenger.setAction("shallWeGoIsNoMore");
                    sendBroadcast(stopMessenger);
                    stopForeground(true);
                    stopSelf();
                }, Throwable::printStackTrace);
                queue.add(request);
                break;
            }

            case "START_SERVICE": {
                rideId = intent.getIntExtra("rideId", 0);
                String notificationChannelId = "LocationChannel";
                String notificationChannelName = "ShallWeGo Notification Channel";
                NotificationChannel channel = new NotificationChannel(notificationChannelId, notificationChannelName, NotificationManager.IMPORTANCE_HIGH);
                channel.setLightColor(Color.BLUE);
                channel.enableLights(true);
                NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
                builder.setOngoing(true).setContentTitle("Sei live!").setPriority(NotificationManager.IMPORTANCE_HIGH).setCategory(Notification.CATEGORY_SERVICE).setSmallIcon(R.drawable.ic_add_location_alt_white_24dp);
                Intent stopper = new Intent(this, LiveTracking.class); // can't come up with a better name :)
                stopper.setAction("STOP_SERVICE");
                builder.addAction(R.drawable.ic_done_all_white_24dp, "È abbastanza", PendingIntent.getService(this, 0, stopper, PendingIntent.FLAG_CANCEL_CURRENT));
                Notification notification = builder.build();
                startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
                fusedLocationClient.requestLocationUpdates(fusedLocationRequest, callbackMethod, Looper.getMainLooper());
                break;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
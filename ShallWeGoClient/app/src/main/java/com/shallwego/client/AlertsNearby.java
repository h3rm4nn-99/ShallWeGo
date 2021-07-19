package com.shallwego.client;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;

import static com.google.android.gms.location.LocationRequest.*;

public class AlertsNearby extends AppCompatActivity {
    private Location currentLocation = null;
    private FusedLocationProviderClient locationClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts_nearby);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
        getSupportActionBar().setTitle("Avvisi nella tua zona");
        SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        LinearLayout container = findViewById(R.id.container);
        run(container);
    }

    @SuppressLint("MissingPermission") //already got on first run
    private void run(LinearLayout container) {
        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Recupero gli eventi rilevanti nella tua zona...", true);
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.getToken()).addOnSuccessListener((location) -> {
            RequestQueue queue = Volley.newRequestQueue(AlertsNearby.this);
            StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getAlertsNearby/" + location.getLatitude() +
                    "," + location.getLongitude(), (response) -> {
                JsonArray jsonResponse = (JsonArray) JsonParser.parseString(response);
                int index = 0;
                for(JsonElement element: jsonResponse) {
                    JsonObject currentAlert = (JsonObject) element;
                    double latitude = Double.parseDouble(currentAlert.get("latitude").toString().replace("\"", ""));
                    double longitude = Double.parseDouble(currentAlert.get("longitude").toString().replace("\"", ""));

                    View view = getLayoutInflater().inflate(R.layout.evento_general_cardview, null);
                    MaterialCardView materialCardView = view.findViewById(R.id.eventCardview);
                    MapView map = view.findViewById(R.id.alertMap);
                    MaterialTextView eventType = view.findViewById(R.id.eventType);
                    MaterialTextView timeValid = view.findViewById(R.id.timeValid);
                    MaterialTextView linesAffected = view.findViewById(R.id.linesAffected);
                    MaterialTextView place = view.findViewById(R.id.eventPlace);
                    place.setSelected(true);

                    materialCardView.setOnClickListener((view1) -> {
                        Intent i = new Intent(AlertsNearby.this, EventDetails.class);
                        i.putExtra("eventId", currentAlert.get("id").toString().replace("\"", ""));
                        startActivity(i);
                    });
                    place.setText(currentAlert.get("place").toString().replace("\"", ""));
                    eventType.setText(currentAlert.get("type").toString().replace("\"", ""));
                    timeValid.setText(currentAlert.get("timeValid").toString().replace("\"", ""));
                    linesAffected.setText(currentAlert.get("linesAffectedPreview").toString().replace("\"", ""));

                    setUpMap(map, latitude, longitude);
                    container.addView(view, index++);
                }
                dialog.dismiss();

            }, (error) -> {
                Toast.makeText(AlertsNearby.this, error.toString(), Toast.LENGTH_SHORT).show();
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setMessage("Controlla la tua connessione ad Internet e riprova!")
                        .setPositiveButton("Ho capito!", (dialog12, which) -> AlertsNearby.this.finish()).show();
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                dialog.dismiss();
            });
            queue.add(request);
        }).addOnFailureListener((exception) -> {
            Toast.makeText(AlertsNearby.this, exception.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Non è stato possibile determinare la tua posizione al momento. Riprova successivamente")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> AlertsNearby.this.finish()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });
    }

    private void setUpMap(MapView map, double latitude, double longitude) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setOnTouchListener((v, event) -> true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);
        map.getOverlays().add(new CopyrightOverlay(this));
        MapController mapController = (MapController) map.getController();
        mapController.setZoom(17.5);
        GeoPoint eventPlace = new GeoPoint(latitude, longitude);
        Marker marker = new Marker(map);
        marker.setPosition(eventPlace);
        marker.setInfoWindow(null);
        map.getOverlays().add(marker);
        mapController.setCenter(eventPlace);
        map.invalidate();
    }
}
package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class RideDetails extends AppCompatActivity {

    private MaterialTextView crowdingValue, acValue, validatingValue, locationValue;
    private ListView notesValue;
    private MapView map;
    private ExtendedFloatingActionButton fab;
    private int rideId;
    private Drawable unwrappedCrowding;
    private Drawable wrappedGreen, wrappedYellow, wrappedRed;
    private ImageView crowdingImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);
        unwrappedCrowding = AppCompatResources.getDrawable(this, R.drawable.ic_groups_white_24dp);
        wrappedGreen = DrawableCompat.wrap(unwrappedCrowding);
        DrawableCompat.setTint(wrappedGreen, Color.GREEN);
        wrappedRed = DrawableCompat.wrap(unwrappedCrowding);
        DrawableCompat.setTint(wrappedRed, Color.RED);
        wrappedYellow = DrawableCompat.wrap(unwrappedCrowding);
        DrawableCompat.setTint(wrappedYellow, Color.YELLOW);

        getSupportActionBar().setTitle("Dettagli Corsa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        rideId = intent.getIntExtra("rideId", 0);
        crowdingValue = findViewById(R.id.affollamentoValue);
        crowdingImageView = findViewById(R.id.affollamentoImageView);
        acValue = findViewById(R.id.ACValue);
        validatingValue = findViewById(R.id.VMValue);
        locationValue = findViewById(R.id.locationValue);
        notesValue = findViewById(R.id.notesValues);
        fab = findViewById(R.id.fab);
        map = findViewById(R.id.map);
        populateFields(); // on first run

        fab.setOnClickListener((view) -> {
            populateFields(); // again!
        });


    }


    private void populateFields() {
        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/rideById/" + rideId, (response) -> {
            JsonObject incomingRide = (JsonObject) JsonParser.parseString(response);

            String title = incomingRide.get("lineIdentifier").getAsString() + " - " + incomingRide.get("destination").getAsString();
            getSupportActionBar().setTitle(title);

            int crowding = incomingRide.get("crowding").getAsInt();

            switch (crowding) {
                case 1: {
                    crowdingValue.setText("Non affollata");
                    crowdingValue.setTextColor(Color.GREEN);
                    crowdingImageView.setImageDrawable(wrappedGreen);
                }
                case 2: {
                    crowdingValue.setText("Affollata");
                    crowdingValue.setTextColor(Color.YELLOW);
                    crowdingImageView.setImageDrawable(wrappedYellow);
                }
                case 3: {
                    crowdingValue.setText("Molto affollata");
                    crowdingValue.setTextColor(Color.RED);
                    crowdingImageView.setImageDrawable(wrappedRed);
                }

                default: {
                    crowdingValue.setText("Non disponibile");
                    crowdingValue.setTextColor(Color.WHITE);
                    crowdingImageView.setImageDrawable(unwrappedCrowding);
                }
            }
            boolean acValueBoolean = incomingRide.get("hasAC").getAsBoolean();
            acValue.setText(acValueBoolean? "Sì" : "No");
            boolean vmValueBoolean = incomingRide.get("hasVM").getAsBoolean();
            validatingValue.setText(vmValueBoolean? "Sì" : "No");

            double lastLatitude = incomingRide.get("lastLatitude").getAsDouble();
            double lastLongitude = incomingRide.get("lastLongitude").getAsDouble();

            locationValue.setText(incomingRide.get("address").getAsString());

            setUpMap(lastLatitude, lastLongitude);

            JsonArray notes = incomingRide.get("notes").getAsJsonArray();
            ArrayList<String> notesArray = new ArrayList<>();
            notes.forEach((note) -> notesArray.add(note.getAsString()));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.provincia_listitem_layout, notesArray);
            adapter.notifyDataSetChanged();
            notesValue.setAdapter(adapter);

            dialog.dismiss();

        }, error -> {
            Toast.makeText(RideDetails.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogCompanies = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> RideDetails.this.finish()).show();
            alertDialogCompanies.setCancelable(false);
            alertDialogCompanies.setCanceledOnTouchOutside(false);
        });

        queue.add(request);
    }

    private void setUpMap(double latitude, double longitude) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setOnTouchListener((v, event) -> true);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);
        map.getOverlays().add(new CopyrightOverlay(this));

        MapController mapController = (MapController) map.getController();
        mapController.setZoom(17.5);
        GeoPoint stopPlace = new GeoPoint(latitude, longitude);
        Marker marker = new Marker(map);
        marker.setPosition(stopPlace);
        marker.setInfoWindow(null);
        marker.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_directions_bus_white_24dp));
        map.getOverlays().add(marker);
        mapController.setCenter(stopPlace);
        map.invalidate();
    }
}
package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.WrappedDrawable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EventDetails extends AppCompatActivity {

    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
        Intent i = getIntent(); // incoming intent
        eventId = Integer.parseInt(i.getStringExtra("eventId"));
        populateLayout(eventId);
    }

    private void populateLayout(int eventId) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY);
        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/eventById/" + eventId, (response) -> {
            JsonObject responseJson = (JsonObject) JsonParser.parseString(response);
            String latitude = responseJson.get("latitude").toString().replace("\"", "");
            String longitude = responseJson.get("longitude").toString().replace("\"", "");

            MapView map = findViewById(R.id.mappaEvento);
            setUpMap(map, latitude, longitude);

            MaterialTextView eventType = findViewById(R.id.tipoEventoValue);
            MaterialTextView validity = findViewById(R.id.durataValue);

            MaterialTextView place = findViewById(R.id.posizioneValue);
            place.setSelected(true);

            MaterialTextView notes = findViewById(R.id.noteValue);

            place.setText(responseJson.get("place").toString().replace("\"", ""));
            eventType.setText(responseJson.get("type").toString().replace("\"", ""));

            String validityStart = responseJson.get("validityStart").toString().replace("\"", "");
            Date validityEnd = null;
            try {
                validityEnd = formatter.parse(responseJson.get("validityEnd").toString().replace("\"", ""));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            boolean inProgress = validityEnd.after(new Date());

            if (inProgress) {
                ImageView durataIv = findViewById(R.id.durataIV);
                Drawable validityImageViewDrawable = durataIv.getDrawable();
                Drawable wrappedValidityImageViewDrawable = DrawableCompat.wrap(validityImageViewDrawable);
                MaterialTextView validityText = findViewById(R.id.durataText);
                validityText.setTextColor(Color.RED);
                DrawableCompat.setTint(wrappedValidityImageViewDrawable, Color.RED);
                validity.setTextColor(Color.RED);
                validity.setText(validityStart + " - in corso");
            } else {
                validity.setText(validityStart + " - " + responseJson.get("validityEnd").toString().replace("\"", ""));
            }

            notes.setText(responseJson.get("description").toString().replace("\"", ""));

            LayoutInflater inflater = getLayoutInflater();
            LinearLayout container = findViewById(R.id.containerEvento);

            HashMap<String, List<String>> lines = new HashMap<>();
            JsonArray linesJson = responseJson.getAsJsonArray("linesAffected");
            for (JsonElement currentLine : linesJson) {
                JsonObject object = (JsonObject) currentLine;
                String company = object.get("companyName").toString().replace("\"", "");

                if (lines.get(company) == null) {
                    lines.put(company, new ArrayList<>());
                }
                lines.get(company).add(object.get("lineIdentifier").toString().replace("\"", "") + " " + object.get("destination").toString().replace("\"", ""));
            }

            int index = 0;
            for (String company : lines.keySet()) {
                View v = inflater.inflate(R.layout.stop_details_company_lines_layout, null);
                ChipGroup chipGroup = v.findViewById(R.id.linesChipGroup);

                MaterialTextView companyLabel = v.findViewById(R.id.companyName);
                companyLabel.setText(company);

                List<String> companyLines = lines.get(company);

                for (String lineName : companyLines) {
                    Chip chip = new Chip(this);
                    chip.setText(lineName);
                    chip.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
                    chipGroup.addView(chip);
                }

                container.addView(v, index++);
            }
            dialog.dismiss();
        }, (error) -> {
            Toast.makeText(EventDetails.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> EventDetails.this.finish()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });

        queue.add(request);
    }

    private void setUpMap(MapView map, String latitude, String longitude) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setOnTouchListener((v, event) -> true);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);
        map.getOverlays().add(new CopyrightOverlay(this));
        MapController mapController = (MapController) map.getController();
        mapController.setZoom(17.5);
        GeoPoint stopPlace = new GeoPoint(Double.parseDouble(latitude), Double.parseDouble(longitude));
        Marker marker = new Marker(map);
        marker.setPosition(stopPlace);
        marker.setInfoWindow(null);
        map.getOverlays().add(marker);
        mapController.setCenter(stopPlace);
        map.invalidate();
    }
}
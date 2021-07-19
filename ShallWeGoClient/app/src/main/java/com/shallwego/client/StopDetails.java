package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.graphics.drawable.DrawableCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class StopDetails extends AppCompatActivity {

    private int stopId;
    private ImageView affollamentoImageView;
    private TextView affollamentoLabel, affollamentoValue;
    private int currentCrowding;
    private boolean isFavorite;
    private MenuItem favoriteToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
        Intent i = getIntent(); // incoming intent
        stopId = Integer.parseInt(i.getStringExtra("stopId"));
        populateLayout(stopId);
    }

    public void populateLayout(int stopId) {
        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getStopDetails/" + stopId, response -> {
            JsonObject responseJson = (JsonObject) JsonParser.parseString(response);
            getSupportActionBar().setTitle(responseJson.get("stopName").toString().replace("\"", ""));
            String latitude = responseJson.get("latitude").toString().replace("\"", "");
            String longitude = responseJson.get("longitude").toString().replace("\"", "");
            currentCrowding = Integer.parseInt(responseJson.get("crowding").toString().replace("\"", ""));
            boolean hasTimeTables = Boolean.parseBoolean(responseJson.get("hasTimeTables").toString().replace("\"", ""));
            boolean hasShelter = Boolean.parseBoolean(responseJson.get("hasShelter").toString().replace("\"", ""));
            boolean hasStopSign = Boolean.parseBoolean(responseJson.get("hasStopSign").toString().replace("\"", ""));

            MapView stopMap = findViewById(R.id.mappaFermata);
            setUpMap(stopMap, latitude, longitude);

            setCrowding();

            MaterialTextView quadriOrariValue = findViewById(R.id.quadriOrariValue);
            MaterialTextView pensilinaValue = findViewById(R.id.pensilinaValue);
            MaterialTextView palinaAziendale = findViewById(R.id.palinaAziendaleValue);
            quadriOrariValue.setText(hasTimeTables? "Sì" : "No");
            pensilinaValue.setText(hasShelter? "Sì" : "No");
            palinaAziendale.setText(hasStopSign? "Sì" : "No");

            HashMap<String, List<String>> lines = new HashMap<>();
            JsonArray linesJson = responseJson.getAsJsonArray("lines");
            for (JsonElement currentLine : linesJson) {
                JsonObject object = (JsonObject) currentLine;
                String company = object.get("companyName").toString().replace("\"", "");

                if (lines.get(company) == null) {
                    lines.put(company, new ArrayList<>());
                }
                lines.get(company).add(object.get("lineIdentifier").toString().replace("\"", "") + " " + object.get("destination").toString().replace("\"", ""));
            }

            LayoutInflater inflater = getLayoutInflater();
            LinearLayout container = findViewById(R.id.containerFermata);

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

            MaterialCardView editCardView = findViewById(R.id.mainLayoutAffollamento);
            addEditOnClickListener(editCardView);

            dialog.dismiss();
        }, error -> {
            Toast.makeText(StopDetails.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> StopDetails.this.finish()).show();
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

    private void setCrowding() {
        affollamentoLabel = findViewById(R.id.affollamentoText);
        affollamentoImageView = findViewById(R.id.affollamentoImageView);
        affollamentoValue = findViewById(R.id.affollamentoValue);
        Drawable drawable = affollamentoImageView.getDrawable();
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);

        if (currentCrowding == 2) {
            DrawableCompat.setTint(wrappedDrawable, Color.RED);
            affollamentoImageView.setImageDrawable(wrappedDrawable);
            affollamentoLabel.setTextColor(Color.RED);
            affollamentoValue.setText("Molto Affollata");
            affollamentoValue.setTextColor(Color.RED);
        } else if (currentCrowding == 1) {
            DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#cc7722"));
            affollamentoImageView.setImageDrawable(wrappedDrawable);
            affollamentoLabel.setTextColor(Color.parseColor("#cc7722"));
            affollamentoValue.setText("Affollata");
            affollamentoValue.setTextColor(Color.parseColor("#cc7722"));
        } else {
            DrawableCompat.setTint(wrappedDrawable, Color.GREEN);
            affollamentoImageView.setImageDrawable(wrappedDrawable);
            affollamentoLabel.setTextColor(Color.GREEN);
            affollamentoValue.setText("Non Affollata");
            affollamentoValue.setTextColor(Color.GREEN);
        }

        SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
        String time = parser.format(new Date());
        affollamentoValue.setText(affollamentoValue.getText().toString() + " (ultimo agg. alle " + time + ")");
    }

    private void addEditOnClickListener(MaterialCardView edit) {
        edit.setOnClickListener(v -> {
            View dialogLayout = getLayoutInflater().inflate(R.layout.edit_crowding_dialog_layout, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(StopDetails.this);
            builder.setView(dialogLayout);
            builder.setPositiveButton("Conferma", null);
            builder.setNegativeButton("Annulla", (dialog, which) -> {
                dialog.dismiss();
            });
            Slider slider = dialogLayout.findViewById(R.id.newCrowdingSlider);
            slider.setValue(currentCrowding);
            MaterialTextView newCrowdingValueTextView = dialogLayout.findViewById(R.id.newCrowdingValue);

            updateDialogLabel(newCrowdingValueTextView);

            slider.addOnChangeListener((slider1, value, fromUser) -> {
                currentCrowding = (int) value;
                updateDialogLabel(newCrowdingValueTextView);

            });

            AlertDialog dialog = builder.show();
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(positiveButton -> {
                updateCrowdingOnServer(stopId, currentCrowding);
                dialog.dismiss();
            });
        });

    }


    private void updateCrowdingOnServer(int stopId, int newCrowding) {
        Toast.makeText(this, "" + newCrowding, Toast.LENGTH_SHORT).show();
        ProgressDialog dialog = ProgressDialog.show(StopDetails.this, "",
                "Attendere prego...", true);
        RequestQueue queue = Volley.newRequestQueue(StopDetails.this);
        StringRequest request = new StringRequest(Request.Method.PUT, IpAddress.SERVER_IP_ADDRESS + "/api/updateStopCrowding/" + stopId + "/" + newCrowding, response -> {
            currentCrowding = Integer.parseInt(response);
            setCrowding();
            dialog.dismiss();

        }, error -> {
            Toast.makeText(StopDetails.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(StopDetails.this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> StopDetails.this.finish()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });
        queue.add(request);
    }

    private void updateDialogLabel(MaterialTextView newCrowdingValueTextView) {
        if (currentCrowding == 2) {
            newCrowdingValueTextView.setText("Molto affollata");
        } else if (currentCrowding == 1) {
            newCrowdingValueTextView.setText("Affollata");
        } else {
            newCrowdingValueTextView.setText("Non affollata");
        }
    }

    private void refreshCrowding() {
        ProgressDialog dialog = ProgressDialog.show(StopDetails.this, "",
                "Attendere prego...", true);
        RequestQueue queue = Volley.newRequestQueue(StopDetails.this);
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getStopCrowding/" + stopId, response -> {
            currentCrowding = Integer.parseInt(response);
            setCrowding();
            dialog.dismiss();

        }, error -> {
            Toast.makeText(StopDetails.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(StopDetails.this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> StopDetails.this.finish()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });
        queue.add(request);
    }

    private void toggleFavorite() {

        isFavorite = !isFavorite;

        if (isFavorite) {   //Necessario per evitare situazioni di inconsistenza dovute ad errori di rete.
            addToFavorite(stopId);
        } else {
            removeFromFavorites(stopId);
        }
    }

    public void addToFavorite(int stopId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.PUT, IpAddress.SERVER_IP_ADDRESS + "/api/addToFavorites/" + MainActivity.userName + "/" + stopId, (response) -> {
            updateFavoriteIcon(Boolean.parseBoolean(response));
        }, (error) -> {});
        queue.add(request);
    }

    public void removeFromFavorites(int stopId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.DELETE, IpAddress.SERVER_IP_ADDRESS + "/api/removeFromFavorites/" + MainActivity.userName + "/" + stopId, (response) -> {
            updateFavoriteIcon(Boolean.parseBoolean(response));
        }, (error) -> {});
        queue.add(request);
    }

    public void updateFavoriteIcon(boolean newValue) {
        if (newValue) {
            favoriteToggle.setIcon(R.drawable.ic_favorite_white_24dp);
        } else {
            favoriteToggle.setIcon(R.drawable.ic_favorite_border_white_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stopdetailsmenu, menu);
        favoriteToggle = menu.getItem(1);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            RequestQueue queue = Volley.newRequestQueue(StopDetails.this);
            StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/isFavorite/" + MainActivity.userName + "/" + stopId, (response) -> {

                isFavorite = Boolean.parseBoolean(response);

                if (isFavorite) {
                    runOnUiThread(() -> favoriteToggle.setIcon(R.drawable.ic_favorite_white_24dp));
                }

            }, (error) -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Errore di rete!", Toast.LENGTH_SHORT).show();
                });
            });

            queue.add(request);
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.finish();
                return true;
            }

            case R.id.refresh: {
                refreshCrowding();
                break;
            }

            case R.id.favorite: {
                toggleFavorite();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
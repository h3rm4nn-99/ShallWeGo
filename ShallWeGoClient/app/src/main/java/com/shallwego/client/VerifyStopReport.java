package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.*;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VerifyStopReport extends AppCompatActivity {
    private double latitude, longitude;
    private int pendingId;
    private ImageView yes, no;
    private int currentVote, initialSum;
    private TextView counter;
    private TextInputEditText stopName;
    private RadioGroup timeTables, stopSign, shelter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_stop_report);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
        Intent incoming = getIntent();
        latitude = incoming.getDoubleExtra("latitude", 0d);
        longitude = incoming.getDoubleExtra("longitude", 0d);
        pendingId = incoming.getIntExtra("pendingId", 0);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        counter = findViewById(R.id.counter);
        stopName = findViewById(R.id.stopEditText);
        timeTables = findViewById(R.id.radioGroupQuadriOrari);
        stopSign = findViewById(R.id.radioGroupPalina);
        shelter = findViewById(R.id.radioGroupPensilina);

        MapView map = findViewById(R.id.mapPendingReport);
        setUpMap(map);

        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getReportDetails/" + pendingId + "/" +  MainActivity.userName, (response) -> {
            JsonObject object = (JsonObject) JsonParser.parseString(response);
            updateColor(object.get("userVote").getAsInt(), true);
            initialSum = object.get("sumOfVotes").getAsInt();
            counter.setText(Integer.toString(initialSum));
            boolean hasShelter = object.get("hasShelter").getAsBoolean();
            boolean hasTimeTables = object.get("hasTimeTables").getAsBoolean();
            boolean hasStopSign = object.get("hasStopSign").getAsBoolean();
            String stopNameString = object.get("stopName").getAsString();
            stopName.setText(stopNameString);
            JsonArray linesJsonArray = object.get("reachableFromThisStop").getAsJsonArray();

            HashMap<String, List<String[]>> lines = new HashMap<>();
            for (JsonElement element: linesJsonArray) {
                JsonObject lineJsonObject = (JsonObject) element;
                String company = lineJsonObject.get("companyName").getAsString();

                if (lines.get(company) == null) {
                    lines.put(company, new ArrayList<>());
                }

                String[] nameAndDestinations = new String[2];
                nameAndDestinations[0] = lineJsonObject.get("lineIdentifier").getAsString();
                String destinations =  "";
                for (JsonElement destination: (JsonArray) lineJsonObject.get("destinations")) {
                    destinations += destination.getAsString();
                }
                nameAndDestinations[1] = destinations;
                lines.get(company).add(nameAndDestinations);
            }

            LayoutInflater inflater = getLayoutInflater();
            LinearLayout container = findViewById(R.id.linesContainer);

            int index = 0;
            for (String company : lines.keySet()) {
                View v = inflater.inflate(R.layout.stop_details_company_lines_layout, null);
                ChipGroup chipGroup = v.findViewById(R.id.linesChipGroup);

                MaterialTextView companyLabel = v.findViewById(R.id.companyName);
                companyLabel.setText(company);

                List<String[]> companyLines = lines.get(company);

                for (String[] lineName : companyLines) {
                    Chip chip = new Chip(this);
                    chip.setText(lineName[0] + " " + lineName[1]);
                    chip.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
                    chipGroup.addView(chip);
                }

                container.addView(v, index++);
            }

            if (hasShelter) {
                shelter.check(R.id.shelterYes);
            } else {
                shelter.check(R.id.shelterNo);
            }

            if (hasStopSign) {
                stopSign.check(R.id.stopSignYes);
            } else {
                stopSign.check(R.id.stopSignNo);
            }

            if (hasTimeTables) {
                timeTables.check(R.id.timeTablesYes);
            } else {
                timeTables.check(R.id.timeTablesNo);
            }

            dialog.dismiss();
        }, (error) -> {
            Toast.makeText(VerifyStopReport.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> VerifyStopReport.this.finish()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });

        queue.add(request);
    }

    private void setUpMap(MapView map) {
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);
        map.getOverlays().add(new CopyrightOverlay(this));
        MapController mapController = (MapController) map.getController();
        mapController.setZoom(17.5);
        GeoPoint stopPlace = new GeoPoint(latitude, longitude);
        Marker marker = new Marker(map);
        marker.setPosition(stopPlace);
        marker.setInfoWindow(null);
        map.getOverlays().add(marker);
        mapController.setCenter(stopPlace);
        map.invalidate();
    }

    public void goToStreetView(View view) {
        Uri gmapsStreetView = Uri.parse("google.streetview:cbll="+ latitude + "," + longitude + "&cbp=0,30,0,0,-15");
        Intent i = new Intent(Intent.ACTION_VIEW, gmapsStreetView);
        try {
            getPackageManager().getPackageInfo("com.google.android.apps.maps", 0);
            i.setPackage("com.google.android.apps.maps");
            startActivity(i);
        } catch (PackageManager.NameNotFoundException e) {}
    }

    private void updateColor(int newVote, boolean firstRun) {
        currentVote = newVote;

        if (currentVote == 1) {
            yes.getDrawable().setTint(Color.GREEN);
            no.getDrawable().setTint(Color.WHITE);
        } else if (currentVote == -1) {
            no.getDrawable().setTint(Color.RED);
            yes.getDrawable().setTint(Color.WHITE);
        }
        if (!firstRun) {
            int sum = Integer.parseInt(counter.getText().toString());
            sum += currentVote;
            counter.setText(Integer.toString(sum));
        }
    }

    public void submitVerification(View view) {
        int vote = Integer.parseInt(view.getTag().toString());
        updateColor(vote, false);

        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.PUT, IpAddress.SERVER_IP_ADDRESS + "/api/verifyReport/" + MainActivity.userName + "/" + pendingId + "/" + vote, (response) -> {
            dialog.dismiss();
            int responseInt = Integer.parseInt(response);

            switch (responseInt) {
                case 1: {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setMessage("Con il tuo voto la segnalazione è stata approvata! Devi esserne fiero.")
                            .setPositiveButton("Ho capito!", (dialog1, which) -> VerifyStopReport.this.finish()).show();
                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    break;
                }

                case 0: {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setMessage("Il tuo voto è stato registrato correttamente.")
                            .setPositiveButton("Ho capito!", (dialog1, which) -> dialog1.dismiss()).show();
                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    break;
                }

                case -1: {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setMessage("Col tuo voto, la segnalazione è stata respinta.")
                            .setPositiveButton("Ho capito!", (dialog1, which) -> VerifyStopReport.this.finish()).show();
                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    break;
                }
            }
        }, (error) -> {
            Toast.makeText(VerifyStopReport.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> VerifyStopReport.this.finish()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });
        q.add(request);
    }
}
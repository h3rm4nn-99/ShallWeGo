package com.shallwego.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.preference.PreferenceManager;
import android.view.*;

import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.text.DecimalFormat;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class MainActivity extends AppCompatActivity {
    private ActionBarDrawerToggle toggle;
    private SharedPreferences preferences;
    private NavigationView navView;
    private FusedLocationProviderClient locationClient;
    private MapController mapController;
    private MapView map;
    private Marker currentLocationMarker = null;
    private Marker outdatedLocationMarker = null;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    public static String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        preferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        boolean skipIntro = preferences.getBoolean("skipIntro", false);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        userName = preferences.getString("user", "placeholder");
        if (!skipIntro) {
            Intent i = new Intent(this, IntroSlideShow.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(i, 101);
        } else if (!isLoggedIn) {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else {
            Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
            Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
            setContentView(R.layout.activity_main);
            DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
            navView = findViewById(R.id.navView);
            toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.open, R.string.close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            configureSpeedDial();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
            navView.setNavigationItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.logout: {
                        logout();
                        break;
                    }
                    case R.id.myReports: {
                        Intent i = new Intent(MainActivity.this, MyReportsActivity.class);
                        startActivity(i);
                        break;
                    }
                    case R.id.favorite_stops: {
                        Intent i = new Intent(MainActivity.this, FavoriteStops.class);
                        startActivity(i);
                        break;
                    }
                    case R.id.alerts: {
                        Intent i = new Intent(MainActivity.this, AlertsNearby.class);
                        startActivity(i);
                        break;
                    }
                    case R.id.verifyReports: {
                        Intent i = new Intent(MainActivity.this, VerifyReports.class);
                        startActivity(i);
                        break;
                    }
                }
                return true;
            });

            setUpHeader();

            map = findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
            map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            map.setMultiTouchControls(true);
            map.getOverlays().add(new CopyrightOverlay(this));
            mapController = (MapController) map.getController();
            mapController.setZoom(18.5);

            MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    Intent i = new Intent(MainActivity.this, StopReportActivity.class);
                    i.putExtra("latitude", p.getLatitude());
                    i.putExtra("longitude", p.getLongitude());
                    startActivity(i);
                    return true;
                }
            });
            map.getOverlays().add(events);

            setStartPoint();
            acquireLocationForMap();


        }
    }

    private void configureSpeedDial() {
        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.fabmenu);

        speedDialView.setOnActionSelectedListener(actionItem -> {
            switch (actionItem.getId()) {
                case R.id.currentLocationGetter: {
                    acquireLocationForMap();
                    break;
                }
                case R.id.report: {
                    acquireLocationForReport();
                    break;
                }
            }

            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 101) && resultCode == Activity.RESULT_OK) {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("user");
        editor.remove("karma");
        editor.remove("isLoggedIn");
        editor.putBoolean("isLoggedIn", false);
        editor.commit();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
        MainActivity.this.finish();
    }

    private void setUpHeader() {
        View v = navView.getHeaderView(0);
        MaterialTextView usernameTextView = v.findViewById(R.id.usernameTextView);
        usernameTextView.setText(preferences.getString("user", "placeholder"));
        MaterialTextView karmaTextView = v.findViewById(R.id.karmaTextView);
        double karma = Double.parseDouble(preferences.getString("karma", ""));
        DecimalFormat formatter = new DecimalFormat("#.##");
        karmaTextView.setText(formatter.format(karma));
    }

    private void setStartPoint() {
        String lastKnownLatitude = preferences.getString("lastKnownLatitude", "40.7720081");
        String lastKnownLongitude = preferences.getString("lastKnownLongitude", "14.791179747427275");
        GeoPoint starter = new GeoPoint(Double.parseDouble(lastKnownLatitude), Double.parseDouble(lastKnownLongitude));
        mapController.setCenter(starter);
        outdatedLocationMarker = new Marker(map);
        outdatedLocationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_disabled_black_48dp));
        outdatedLocationMarker.setInfoWindow(null);
        outdatedLocationMarker.setPosition(starter);
        outdatedLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        map.getOverlays().add(outdatedLocationMarker);
        map.invalidate();
    }

    @SuppressLint("MissingPermission") //permission already granted on first launch
    private void acquireLocationForMap() {
        Location location = null;
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken()).addOnSuccessListener(this, location1 -> {
            double currentLatitude = location1.getLatitude();
            double currentLongitude = location1.getLongitude();
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("lastKnownLatitude");
            editor.remove("lastKnownLongitude");

            editor.putString("lastKnownLatitude", String.valueOf(currentLatitude));
            editor.putString("lastKnownLongitude", String.valueOf(currentLongitude));
            editor.commit();

            if (map.getOverlays().contains(outdatedLocationMarker)) {
                map.getOverlays().remove(outdatedLocationMarker);
            }

            if (currentLocationMarker != null) {
                map.getOverlays().remove(currentLocationMarker);
            }


            GeoPoint currentLocation = new GeoPoint(currentLatitude, currentLongitude);
            currentLocationMarker = new Marker(map);
            currentLocationMarker.setInfoWindow(null);
            currentLocationMarker.setIcon(getDrawable(R.drawable.ic_gps_fixed_white_48dp));
            currentLocationMarker.setPosition(currentLocation);
            currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            map.getOverlays().add(currentLocationMarker);
            mapController.setCenter(currentLocation);
            map.invalidate();

        });
    }

    @SuppressLint("MissingPermission") //already got on first run
    private void acquireLocationForReport() {
        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Aggiornamento della posizione in corso...", true);
        Location location = null;
        if (locationClient == null) {
            locationClient = LocationServices.getFusedLocationProviderClient(this);
        }
            locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken()).addOnSuccessListener((location1) -> {
                double currentLatitude = location1.getLatitude();
                double currentLongitude = location1.getLongitude();
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("lastKnownLatitude");
                editor.remove("lastKnownLongitude");

                editor.putString("lastKnownLatitude", String.valueOf(currentLatitude));
                editor.putString("lastKnownLongitude", String.valueOf(currentLongitude));
                editor.commit();
                showReportTypeChooserDialog(currentLatitude, currentLongitude);
                dialog.dismiss();


            }).addOnFailureListener((error) -> {
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setMessage("Impossibile recuperare la tua posizione precisa in questo momento. Data la natura particolare dell'operazione, essa è strettamente necessaria. Riprova.")
                        .setPositiveButton("Ho capito!", (dialog1, which) -> dialog1.dismiss()).show();
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                dialog.dismiss();
            });
        }

    private void showReportTypeChooserDialog(double currentLatitude, double currentLongitude) {

        View dialogLayout = getLayoutInflater().inflate(R.layout.alert_dialog_report_type, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialogReport = builder.setView(dialogLayout).setNegativeButton("Annulla", (dialog, which) -> {
            dialog.dismiss();
        }).setTitle("Cosa vuoi segnalarci?").show();

        View.OnClickListener listener = (view) -> {
            switch (view.getId()) {
                case R.id.buttonStop: {
                    Intent i = new Intent(MainActivity.this, StopReportActivity.class);
                    i.putExtra("latitude", currentLatitude);
                    i.putExtra("longitude", currentLongitude);
                    startActivity(i);
                    dialogReport.dismiss();
                    break;
                }

                case R.id.buttonLine: {
                    Intent i = new Intent(MainActivity.this, LineReportActivity.class);
                    startActivity(i);
                    dialogReport.dismiss();
                    break;
                }
                case R.id.buttonCompany: {
                    Intent i = new Intent(MainActivity.this, CompanyReportActivity.class);
                    i.putExtra("latitude", currentLatitude);
                    i.putExtra("longitude", currentLongitude);
                    startActivity(i);
                    dialogReport.dismiss();
                    break;
                }
                case R.id.buttonTemporaryEvent: {
                    Intent i = new Intent(MainActivity.this, TemporaryEventReportActivity.class);
                    i.putExtra("latitude", currentLatitude);
                    i.putExtra("longitude", currentLongitude);
                    startActivity(i);
                    dialogReport.dismiss();
                    break;
                }
            }
        };

        LinearLayout stopButton = dialogLayout.findViewById(R.id.buttonStop);
        stopButton.setOnClickListener(listener);
        LinearLayout lineButton = dialogLayout.findViewById(R.id.buttonLine);
        lineButton.setOnClickListener(listener);
        LinearLayout companyButton = dialogLayout.findViewById(R.id.buttonCompany);
        companyButton.setOnClickListener(listener);
        LinearLayout eventButton = dialogLayout.findViewById(R.id.buttonTemporaryEvent);
        eventButton.setOnClickListener(listener);
    }
}
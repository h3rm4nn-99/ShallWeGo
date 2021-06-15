package com.shallwego.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.preference.PreferenceManager;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import org.jetbrains.annotations.NotNull;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        boolean skipIntro = preferences.getBoolean("skipIntro", false);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
            navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.logout: {
                            logout();
                        }
                    }
                    return true;
                }
            });

            setUpHeader();

            map = findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            map.setMultiTouchControls(true);
            map.getOverlays().add(new CopyrightOverlay(this));
            mapController = (MapController) map.getController();
            mapController.setZoom(18.5);

            setStartPoint();
            acquireLocation();


        }
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

    @SuppressLint("MissingPermission") //permession already granted on first launch
    private void acquireLocation() {
        Location location = null;
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken()).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();
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

            }
        });
    }

    @SuppressLint("RestrictedApi")
    public void showPopUpMenu(View view) {
        PopupMenu menu = new PopupMenu(this, findViewById(R.id.floating_action_button));
        MenuInflater menuInflater = menu.getMenuInflater();
        menuInflater.inflate(R.menu.fabmenu, menu.getMenu());

        MenuPopupHelper helper = new MenuPopupHelper(this, (MenuBuilder) menu.getMenu(), findViewById(R.id.floating_action_button));
        helper.setForceShowIcon(true);
        helper.show();

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.currentLocationGetter:
                        acquireLocation();
                }
                return true;
            }
        });

    }
}
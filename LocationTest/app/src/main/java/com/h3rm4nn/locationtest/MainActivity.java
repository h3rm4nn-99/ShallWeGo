package com.h3rm4nn.locationtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class MainActivity extends AppCompatActivity {
    private static ActionBarDrawerToggle toggle;
    private static DrawerLayout drawerLayout;
    private static NavigationView navView;
    private FusedLocationProviderClient locationClient;
    private static MapView map;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private IMapController mapController;
    private FloatingActionButton button;
    private boolean isServiceStarted = true;
    private Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        setContentView(R.layout.activity_main);
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navView = (NavigationView) findViewById(R.id.navView);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        Toast.makeText(MainActivity.this, "Item1 tapped", Toast.LENGTH_SHORT).show();
                        startForegroundService(new Intent(MainActivity.this, LocationService.class));
                        break;
                    case R.id.item2:
                        Toast.makeText(MainActivity.this, "Item2 tapped", Toast.LENGTH_SHORT).show();
                        if (isServiceStarted) {
                            stopService(new Intent(MainActivity.this, LocationService.class));
                        }
                        break;
                    case R.id.item3:
                        Toast.makeText(MainActivity.this, "Item3 tapped", Toast.LENGTH_SHORT).show();
                        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                        StringRequest request = new StringRequest(Request.Method.GET, "http://192.168.1.22/nominatim/search.php?q=Campus di Fisciano", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (myLocation == null) {
                                    return;
                                }
                                if (response.equals("[]")) {
                                    throw new IllegalStateException("Something's very wrong");
                                }
                                System.out.println(response);
                                JSONArray array = null;
                                try {
                                    array = new JSONArray(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                JSONObject obj = null;
                                try {
                                    obj = array.getJSONObject(0);
                                    double latitude = Double.parseDouble(obj.getString("lat"));
                                    double longitude = Double.parseDouble(obj.getString("lon"));
                                    GeoPoint unisa = new GeoPoint(latitude, longitude);
                                    GeoPoint here = new GeoPoint(myLocation);
                                    Polyline line = new Polyline();
                                    line.setWidth(12f);
                                    ArrayList<GeoPoint> points = new ArrayList<>();
                                    points.add(unisa);
                                    points.add(here);
                                    line.setPoints(points);
                                    map.getOverlays().add(line);
                                    map.invalidate();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Errore di Volley! " + error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        queue.add(request);
                        break;
                }
                return true;
            }
        });
        button = findViewById(R.id.floating_action_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Yeah", Snackbar.LENGTH_LONG).setAction("Sicuro?", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mapController.setZoom(9.5);
                        map.invalidate();
                    }
                }).setTextColor(Color.RED).show();
            }
        });
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.getOverlays().add(new CopyrightOverlay(this));
        mapController = map.getController();
        mapController.setZoom(17.0);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 101);
        } else {
            Toast.makeText(this, "Permesso già rilasciato", Toast.LENGTH_SHORT).show();
        }

        locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken()).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                myLocation = location; //make location object globally available
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(startPoint);
                Marker m = new Marker(map);
                m.setPosition(startPoint);
                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                m.setIcon(getDrawable(R.drawable.ic_near_me_24px));
                m.setInfoWindow(new MyMarkerInfoWindow(R.layout.layout_bubble, map, startPoint));
                m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        m.showInfoWindow();
                        return true;
                    }
                });
                map.getOverlays().add(m);
                map.invalidate();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101: {
                Toast.makeText(this, "Permesso rilasciato ora!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }

            default:
                Toast.makeText(this, "Errore!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
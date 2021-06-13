package com.shallwego.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import org.jetbrains.annotations.NotNull;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private static ActionBarDrawerToggle toggle;
    private static SharedPreferences preferences;
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
            NavigationView navView = findViewById(R.id.navView);
            toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.open, R.string.close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.logout: {
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
                    }
                    return true;
                }
            });
            View v = navView.getHeaderView(0);
            MaterialTextView usernameTextView = v.findViewById(R.id.usernameTextView);
            usernameTextView.setText(preferences.getString("user", "placeholder"));
            MaterialTextView karmaTextView = v.findViewById(R.id.karmaTextView);
            double karma = Double.parseDouble(preferences.getString("karma", ""));
            DecimalFormat formatter = new DecimalFormat("#.##");
            karmaTextView.setText(formatter.format(karma));
            MapView map = findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            map.setMultiTouchControls(true);
            map.getOverlays().add(new CopyrightOverlay(this));
            IMapController mapController = map.getController();
            mapController.setZoom(17.0);
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
}
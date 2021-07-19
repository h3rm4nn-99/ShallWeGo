package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Iterator;

public class FavoriteStops extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_stops);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
        getSupportActionBar().setTitle("Le mie fermate preferite");
        SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String user = preferences.getString("user", "");
        LinearLayout container = findViewById(R.id.favContainer);
        populateLayout(user, container);
    }

    public void populateLayout(String user, LinearLayout container) {
        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getFavorites/" + user, response -> {
            JsonArray reportsJson = (JsonArray) JsonParser.parseString(response);
            Iterator<JsonElement> iterator = reportsJson.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                JsonObject currentStop = iterator.next().getAsJsonObject();
                View stopView = getLayoutInflater().inflate(R.layout.fermata_preferita_cardview, null);
                TextView fermataPreferitaIdentifier = stopView.findViewById(R.id.fermata_preferita_identifier);
                fermataPreferitaIdentifier.setText(currentStop.get("stopName").toString().replace("\"", ""));
                String latitudine = currentStop.get("latitude").toString();
                String longitudine = currentStop.get("longitude").toString();
                TextView posizione = stopView.findViewById(R.id.fermata_preferita_posizione);
                posizione.setText(latitudine + ", " + longitudine);
                ImageView dettagli = stopView.findViewById(R.id.dettagliFermataPreferita);
                dettagli.setOnClickListener((view) -> {
                    Intent i = new Intent(FavoriteStops.this, StopDetails.class);
                    i.putExtra("stopId", currentStop.get("stopId").toString().replace("\"", ""));
                    startActivity(i);
                });
                container.addView(stopView, index++);
            }
            dialog.dismiss();
        }, error -> {
            Toast.makeText(FavoriteStops.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> FavoriteStops.this.finish()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });
        q.add(request);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
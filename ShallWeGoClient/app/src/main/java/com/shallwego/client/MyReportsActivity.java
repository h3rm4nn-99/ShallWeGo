package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MyReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
        getSupportActionBar().setTitle("Le mie segnalazioni");
        SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String user = preferences.getString("user", "");
        LinearLayout container = findViewById(R.id.container);
        populateLayout(container, user);

    }

    private void populateLayout(LinearLayout container, String user) {
        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.1.3:8080/api/reports/" + user, response -> {
            JsonArray reportsJson = (JsonArray) JsonParser.parseString(response);
            Iterator<JsonElement> iterator = reportsJson.iterator();
            while (iterator.hasNext()) {
                JsonObject currentReport = iterator.next().getAsJsonObject();
                Toast.makeText(this, currentReport.get("type").toString(), Toast.LENGTH_SHORT).show();
                switch (currentReport.get("type").toString().replace("\"", "")) {
                    case "StopReport": {
                        View stopReportView = getLayoutInflater().inflate(R.layout.fermata_report_cardview, null);
                        TextView nomeFermata = stopReportView.findViewById(R.id.fermata_identifier);
                        nomeFermata.setText(currentReport.get("stopName").toString().replace("\"", ""));
                        TextView dataReport = stopReportView.findViewById(R.id.fermata_date_reported);
                        dataReport.setText(currentReport.get("date").toString().replace("\"", ""));
                        String latitudine = currentReport.get("latitude").toString();
                        String longitudine = currentReport.get("longitude").toString();
                        TextView posizione = stopReportView.findViewById(R.id.fermata_posizione);
                        posizione.setText(latitudine + ", " + longitudine);
                        container.addView(stopReportView, 0);

                    }
                }
            }
            dialog.dismiss();
        }, error -> {
            Toast.makeText(MyReportsActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MyReportsActivity.this.finish();
                        }
                    }).show();
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
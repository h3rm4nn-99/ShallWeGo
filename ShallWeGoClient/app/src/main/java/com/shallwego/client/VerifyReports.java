package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Context;
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
import androidx.cardview.widget.CardView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Iterator;

public class VerifyReports extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_reports);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
        getSupportActionBar().setTitle("Verifica le segnalazioni");
        SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String user = preferences.getString("user", "");
        LinearLayout container = findViewById(R.id.container);
        populateLayout(container, user);

    }

    private void populateLayout(LinearLayout container, String user) {
        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, IpAddress.SERVER_IP_ADDRESS + "/api/pendingReports/" + user, response -> {
            JsonArray reportsJson = (JsonArray) JsonParser.parseString(response);
            Iterator<JsonElement> iterator = reportsJson.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                JsonObject currentReport = iterator.next().getAsJsonObject();
                switch (currentReport.get("type").toString().replace("\"", "")) {
                    case "StopReport": {
                        View stopReportView = getLayoutInflater().inflate(R.layout.fermata_report_cardview, null);
                        TextView nomeFermata = stopReportView.findViewById(R.id.fermata_identifier);
                        nomeFermata.setText(currentReport.get("stopName").toString().replace("\"", ""));
                        TextView dataReport = stopReportView.findViewById(R.id.fermata_date_reported);
                        dataReport.setText(currentReport.get("date").toString().replace("\"", ""));
                        double latitude = currentReport.get("latitude").getAsDouble();
                        double longitude = currentReport.get("longitude").getAsDouble();
                        TextView posizione = stopReportView.findViewById(R.id.fermata_posizione);
                        posizione.setText(latitude + ", " + longitude);
                        ImageView dettagliFermata = stopReportView.findViewById(R.id.dettagliFermata);
                        dettagliFermata.setOnClickListener((view) -> {
                            Intent i = new Intent(VerifyReports.this, VerifyStopReport.class);
                            i.putExtra("reportId", currentReport.get("pendingId").getAsString());
                            i.putExtra("latitude", latitude);
                            i.putExtra("longitude", longitude);
                            startActivity(i);
                        });
                        container.addView(stopReportView, index++);
                        break;
                    }

                    case "LineReport": {
                        View lineReportView = getLayoutInflater().inflate(R.layout.linea_report_cardview, null);
                        TextView nomeLinea = lineReportView.findViewById(R.id.linea_identifier);
                        nomeLinea.setText(currentReport.get("lineIdentifier").toString().replace("\"", ""));
                        TextView dataReport = lineReportView.findViewById(R.id.linea_date_reported);
                        dataReport.setText(currentReport.get("date").toString().replace("\"", ""));
                        TextView nomeCompagnia = lineReportView.findViewById(R.id.linea_compagnia);
                        nomeCompagnia.setText(currentReport.get("companyName").toString().replace("\"", ""));
                        ImageView dettagliLinea = lineReportView.findViewById(R.id.dettagliLinea);
                        dettagliLinea.setOnClickListener((view) -> {
                            Intent i = new Intent(VerifyReports.this, VerifyLineReport.class);
                            i.putExtra("reportId", currentReport.get("pendingId").getAsString());
                            startActivity(i);
                        });
                        container.addView(lineReportView, index++);
                        break;

                    }
                    case "CompanyReport": {
                        View companyReportView = getLayoutInflater().inflate(R.layout.azienda_report_cardview, null, false);
                        TextView nomeAzienda = companyReportView.findViewById(R.id.nome_azienda);
                        nomeAzienda.setText(currentReport.get("companyName").toString().replace("\"", ""));
                        TextView webAzienda = companyReportView.findViewById(R.id.web_azienda);
                        webAzienda.setText(currentReport.get("companyWebsite").toString().replace("\"", ""));
                        companyReportView.setOnClickListener((view) -> {
                            Intent i = new Intent(VerifyReports.this, VerifyCompanyReport.class);
                            i.putExtra("reportId", currentReport.get("pendingId").getAsString());
                            startActivity(i);
                        });
                        container.addView(companyReportView, index++);
                        break;
                    }
                }
            }
            dialog.dismiss();
        }, error -> {
            Toast.makeText(VerifyReports.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> VerifyReports.this.finish()).show();
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


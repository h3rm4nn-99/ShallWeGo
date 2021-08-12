package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shallwego.client.databinding.ActivityCompanyReportBinding;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CompanyReportActivity extends AppCompatActivity {

    private ActivityCompanyReportBinding binding;
    private TextInputLayout companyName, companyWebSite;
    private TextInputEditText companyNameEditText, companyWebSiteEditText;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCompanyReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        companyName = findViewById(R.id.companyNameTextLayout);
        companyWebSite = findViewById(R.id.companyWebSiteLayout);
        companyNameEditText = (TextInputEditText) companyName.getEditText();
        companyWebSiteEditText = (TextInputEditText) companyWebSite.getEditText();
        Intent incoming = getIntent();
        latitude = incoming.getDoubleExtra("latitude", 0.0d);
        longitude = incoming.getDoubleExtra("longitude", 0.0d);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle("Segnala Azienda");
        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener((view) -> {
            submitData();
        });
    }

    private void submitData() {
        if (!validateInput()) {
            return;
        } else {
            JsonObject dataToBeSent = new JsonObject();
            dataToBeSent.addProperty("companyName", companyNameEditText.getText().toString());
            dataToBeSent.addProperty("website", companyWebSiteEditText.getText().toString());
            dataToBeSent.addProperty("latitude", latitude);
            dataToBeSent.addProperty("longitude", longitude);

            String requestBody = dataToBeSent.toString();
            ProgressDialog progressDialog = ProgressDialog.show(this, "",
                    "Attendere prego...", true);
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.PUT, IpAddress.SERVER_IP_ADDRESS + "/api/newCompanyReport/" + MainActivity.userName, (response) -> {
                JsonArray verifiers = (JsonArray) JsonParser.parseString(response);
                ArrayList<String> verifiersArray = new ArrayList<>();
                for (JsonElement element: verifiers) {
                    verifiersArray.add(element.getAsString());
                }
                View v = getLayoutInflater().inflate(R.layout.alert_dialog_after_report, null);
                ListView listView = v.findViewById(R.id.listViewVerifiers);
                AlertDialog.Builder verifiersDialog = new AlertDialog.Builder(this).setView(v);
                listView.setAdapter(new ArrayAdapter<String>(CompanyReportActivity.this, R.layout.provincia_listitem_layout, verifiersArray));
                verifiersDialog.setTitle("Ecco la zona di chi verificherà la tua segnalazione");
                verifiersDialog.setPositiveButton("Capito!", ((dialog, which) -> {
                    dialog.dismiss();
                    CompanyReportActivity.this.finish();
                }));
                progressDialog.dismiss();
                verifiersDialog.show();
            }, (error) -> {
                Toast.makeText(CompanyReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                AlertDialog alertDialog = new AlertDialog.Builder(CompanyReportActivity.this)
                        .setMessage("Controlla la tua connessione ad Internet e riprova!")
                        .setPositiveButton("Ho capito!", (dialog1, which) -> System.out.println() /*LineReportActivity.this.finish()*/).show();
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                progressDialog.dismiss();
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
                }
            };
            RetryPolicy mRetryPolicy = new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            stringRequest.setRetryPolicy(mRetryPolicy);
            queue.add(stringRequest);
        }
    }

    private boolean validateInput() {
        boolean check = true;
        companyName.setError(null);
        companyWebSite.setError(null);
        if (companyNameEditText.getText().toString().isEmpty()) {
            companyName.setError("Inserisci il nome dell'azienda che vuoi segnalare!");
            check = false;
        }

        String website = companyWebSiteEditText.getText().toString();

        if (!website.isEmpty() && website.matches("/^(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})/$")) {
            companyWebSite.setError("Il sito da te inserito non rappresenta un sito web valido!");
            check = false;
        }
        return check;
    }
}
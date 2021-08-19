package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VerifyCompanyReport extends AppCompatActivity {

    private TextInputEditText website, name;
    private int pendingId;
    private ImageView yes, no;
    private int currentVote;
    private TextView counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_company_report);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6200EE")));
        name = findViewById(R.id.companyNameEditText);
        website = findViewById(R.id.companyWebSiteEditText);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        counter = findViewById(R.id.counter);

        Intent i = getIntent();
        pendingId = i.getIntExtra("pendingId", 0);

        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getReportDetails/" + pendingId + "/" +  MainActivity.userName, (response) -> {
            JsonObject object = (JsonObject) JsonParser.parseString(response);
            updateColor(object.get("userVote").getAsInt(), true);
            counter.setText(object.get("sumOfVotes").getAsString());
            name.setText(object.get("companyName").getAsString());
            if (object.has("companyWebSite")) {
                website.setText(object.get("companyWebSite").getAsString());
            }
            dialog.dismiss();
        }, (error) -> {
            Toast.makeText(VerifyCompanyReport.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> VerifyCompanyReport.this.finish()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });

        q.add(request);
    }

    private void updateColor(int newVote, boolean firstRun) {
        currentVote = newVote;
        if (currentVote == 1) {
            Drawable unwrappedDrawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_36dp);
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, Color.GREEN);
            yes.setImageDrawable(wrappedDrawable);
            Drawable unwrappedDrawableNo = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_36dp);
            Drawable wrappedDrawableNo = DrawableCompat.wrap(unwrappedDrawableNo);
            DrawableCompat.setTint(wrappedDrawableNo, Color.WHITE);
            no.setImageDrawable(wrappedDrawableNo);
        } else {
            Drawable unwrappedDrawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_36dp);
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, Color.RED);
            no.setImageDrawable(wrappedDrawable);
            Drawable unwrappedDrawableYes = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_36dp);
            Drawable wrappedDrawableYes = DrawableCompat.wrap(unwrappedDrawableYes);
            DrawableCompat.setTint(wrappedDrawableYes, Color.GREEN);
            yes.setImageDrawable(wrappedDrawableYes);
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
                            .setPositiveButton("Ho capito!", (dialog1, which) -> VerifyCompanyReport.this.finish()).show();
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
                            .setPositiveButton("Ho capito!", (dialog1, which) -> VerifyCompanyReport.this.finish()).show();
                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    break;
                }
            }
        }, (error) -> {
            Toast.makeText(VerifyCompanyReport.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> VerifyCompanyReport.this.finish()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });
        q.add(request);
    }
}
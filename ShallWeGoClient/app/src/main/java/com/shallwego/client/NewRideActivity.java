package com.shallwego.client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.shallwego.client.databinding.ActivityNewRideBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class NewRideActivity extends AppCompatActivity {

    private ActivityNewRideBinding binding;
    private Chip moreInfo;
    private HorizontalScrollView horizontalScrollView;
    private RadioGroup destinationsContainer, ac, validatingMachine;
    private RadioButton acYes, validatingMachineYes;
    private LinearLayout containerNotes;
    private ProgressDialog dialog;
    private TextInputLayout layoutCompany, layoutLine;
    private AutoCompleteTextView editTextCompany, editTextLine;
    private MaterialTextView destinationError, crowdingError;
    private ImageView notCrowded, slightlyCrowded, veryCrowded;
    private int crowding = 0;
    private Drawable unwrapped1, unwrapped2, unwrapped3;
    private Drawable wrapped1, wrapped2, wrapped3;
    private double latitude, longitude;

    private final View.OnClickListener selectCrowding = (view) -> {
        int tag = Integer.parseInt(view.getTag().toString());

        crowding = tag;

        notCrowded.setImageDrawable(unwrapped1);
        slightlyCrowded.setImageDrawable(unwrapped2);
        veryCrowded.setImageDrawable(unwrapped3);

        switch (tag) {
            case 1: {
                notCrowded.setImageDrawable(wrapped1);
                break;
            }

            case 2: {
                slightlyCrowded.setImageDrawable(wrapped2);
                break;
            }

            case 3: {
                veryCrowded.setImageDrawable(wrapped3);
                break;
            }
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle("Nuova corsa");

        Intent incoming = getIntent();
        latitude = incoming.getDoubleExtra("latitude", 0d);
        longitude = incoming.getDoubleExtra("longitude", 0d);

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener((view) -> {
            if (validate()) {

                HashSet<String> notes = parseDetails();
                String[] intentAwareNotes = notes.toArray(new String[0]);
                Intent intent = new Intent();
                intent.putExtra("details", intentAwareNotes);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("lineIdentifier", editTextLine.getText().toString());
                intent.putExtra("companyName", editTextCompany.getText().toString());
                intent.putExtra("crowding", crowding);
                intent.putExtra("airConditioning", ac.getCheckedRadioButtonId() == -1? null: acYes.isChecked());
                intent.putExtra("validatingMachine", validatingMachine.getCheckedRadioButtonId() == -1? null: validatingMachineYes.isChecked());
                RadioButton destination = findViewById(destinationsContainer.getCheckedRadioButtonId());
                intent.putExtra("destination", destination.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        moreInfo = findViewById(R.id.moreInfo);
        moreInfo.setOnClickListener((view) -> {
            addNewRow();
        });
        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        destinationsContainer = findViewById(R.id.destinationsContainer);
        containerNotes = findViewById(R.id.containerNotes);
        layoutCompany = findViewById(R.id.company);
        layoutLine = findViewById(R.id.line);
        editTextCompany = (AutoCompleteTextView) layoutCompany.getEditText();
        editTextLine = (AutoCompleteTextView) layoutLine.getEditText();

        destinationError = findViewById(R.id.destinationsError);
        crowdingError = findViewById(R.id.crowdingError);

        notCrowded = findViewById(R.id.notCrowded);
        slightlyCrowded = findViewById(R.id.slightlyCrowded);
        veryCrowded = findViewById(R.id.veryCrowded);

        notCrowded.setOnClickListener(selectCrowding);
        slightlyCrowded.setOnClickListener(selectCrowding);
        veryCrowded.setOnClickListener(selectCrowding);

        unwrapped1 = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_sentiment_satisfied_white_48dp);
        unwrapped2 = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_sentiment_neutral_white_48dp);
        unwrapped3 = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_sentiment_dissatisfied_white_48dp);

        wrapped1 = DrawableCompat.wrap(unwrapped1);
        DrawableCompat.setTint(unwrapped1, Color.GREEN);

        wrapped2 = DrawableCompat.wrap(unwrapped2);
        DrawableCompat.setTint(unwrapped1, Color.YELLOW);

        wrapped3 = DrawableCompat.wrap(unwrapped3);
        DrawableCompat.setTint(unwrapped3, Color.RED);

        ac = findViewById(R.id.radioGroupAirConditioning);
        validatingMachine = findViewById(R.id.radioGroupTicketValidator);

        acYes = findViewById(R.id.airConditioningYes);
        validatingMachineYes = findViewById(R.id.ticketValidatorYes);

        dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getCompanies", response -> {
            JsonArray companiesJson = (JsonArray) JsonParser.parseString(response);
            Iterator<JsonElement> iterator = companiesJson.iterator();
            ArrayList<String> companies = new ArrayList<>();
            while (iterator.hasNext()) {
                companies.add(iterator.next().toString().replace("\"", ""));
            }
            Collections.sort(companies);
            editTextCompany.setAdapter(new ArrayAdapter<String>(NewRideActivity.this, R.layout.provincia_listitem_layout, companies));
            dialog.dismiss();
            populateDropDownMenus(editTextCompany, editTextLine);


        }, error -> {
            Toast.makeText(NewRideActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogCompanies = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog, which) -> NewRideActivity.this.finish()).show();
            alertDialogCompanies.setCancelable(false);
            alertDialogCompanies.setCanceledOnTouchOutside(false);
        });

        q.add(request);
    }

    private HashSet<String> parseDetails() {
        HashSet<String> notes = new HashSet<>();

        for (int i = 0; i < containerNotes.getChildCount(); i++) {
            LinearLayout currentLayout = (LinearLayout) containerNotes.getChildAt(i);
            CheckBox checkBox = (CheckBox) currentLayout.getChildAt(0);
            if (checkBox.isChecked()) {
                TextInputEditText editText = (TextInputEditText) currentLayout.getChildAt(1);
                if (!editText.getText().toString().isEmpty()) {
                    notes.add(editText.getText().toString());
                }
            }
        }
        return notes;
    }

    private void addNewRow() {
        if (containerNotes.getChildCount() != 0) {
            LinearLayout linearLayout = (LinearLayout) containerNotes.getChildAt(containerNotes.getChildCount() - 1);
            TextInputEditText editText = (TextInputEditText) linearLayout.getChildAt(1);
            if (editText.getText().toString().isEmpty()) {
                return;
            }
        }
        View view = getLayoutInflater().inflate(R.layout.checkbox_with_details, null);
        containerNotes.addView(view, containerNotes.getChildCount());
    }

    private boolean validate() {
        boolean check = true;
        layoutCompany.setError(null);
        layoutLine.setError(null);
        destinationError.setVisibility(View.INVISIBLE);
        crowdingError.setVisibility(View.INVISIBLE);

        if (editTextCompany.getText().toString().isEmpty()) {
            layoutCompany.setError("Seleziona un'azienda");
            check = false;
        }

        if (editTextLine.getText().toString().isEmpty()) {
            layoutLine.setError("Seleziona una linea");
            check = false;
        }

        if (destinationsContainer.getCheckedRadioButtonId() == -1) {
            destinationError.setVisibility(View.VISIBLE);
            check = false;
        }

        if (crowding == 0) {
            crowdingError.setVisibility(View.VISIBLE);
            check = true;
        }

        return check;
    }

    private void populateDropDownMenus(AutoCompleteTextView editTextCompany, AutoCompleteTextView editTextLine) {
        editTextLine.setEnabled(false);
        editTextCompany.setOnItemClickListener((parent, view, position, id) -> {
            ProgressDialog dialogLines = ProgressDialog.show(NewRideActivity.this, "",
                    "Attendere prego...", true);
            String line = (String) parent.getItemAtPosition(position);
            editTextLine.setEnabled(true);
            editTextLine.setText("");
            RequestQueue q1 = Volley.newRequestQueue(getApplicationContext());
            StringRequest request1 = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getCompanyLines/" + editTextCompany.getText().toString(), response -> {
                JsonArray linesJson = (JsonArray) JsonParser.parseString(response);
                Iterator<JsonElement> iterator = linesJson.iterator();
                ArrayList<String> lines = new ArrayList<>();
                while (iterator.hasNext()) {
                    lines.add(iterator.next().toString().replace("\"", ""));
                }
                Collections.sort(lines);
                editTextLine.setAdapter(new ArrayAdapter<String>(NewRideActivity.this, R.layout.provincia_listitem_layout, lines));
                dialogLines.dismiss();
                editTextLine.setOnItemClickListener((parent1, view1, position1, id1) -> {
                    processDestinations((String) parent1.getItemAtPosition(position1), editTextCompany.getText().toString());
                });
            }, error -> {
                Toast.makeText(NewRideActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                AlertDialog alertDialogLines = new AlertDialog.Builder(NewRideActivity.this)
                        .setMessage("Controlla la tua connessione ad Internet e riprova!")
                        .setPositiveButton("Ho capito!", (dialog1, which) -> NewRideActivity.this.finish()).show();
                alertDialogLines.setCancelable(false);
                alertDialogLines.setCanceledOnTouchOutside(false);
                dialogLines.dismiss();

            });
            q1.add(request1);

        });
    }

    public void processDestinations(String selectedLine, String selectedCompany) {
        ProgressDialog dialogDestinations = ProgressDialog.show(NewRideActivity.this, "",
                "Attendere prego...", true);
        horizontalScrollView.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getLineDestinations/" + selectedCompany.replace(" ", "%20") + "/" + selectedLine.replace(" ", "%20"), (response) -> {
            JsonArray responseJsonArray = (JsonArray) JsonParser.parseString(response);
            ArrayList<String> destinations = new ArrayList<>();
            for (JsonElement destination: responseJsonArray) {
                destinations.add(destination.toString().replace("\"", ""));
            }
            destinationsContainer.removeAllViews();
            int i = 0;
            for (String destination: destinations) {
               RadioButton button = new RadioButton(this);
               button.setText(destination);
               destinationsContainer.addView(button, i++);
            }

            dialogDestinations.dismiss();
        }, (error) -> {
            Toast.makeText(NewRideActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogDestinations = new AlertDialog.Builder(NewRideActivity.this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> NewRideActivity.this.finish()).show();
            alertDialogDestinations.setCancelable(false);
            alertDialogDestinations.setCanceledOnTouchOutside(false);
            dialogDestinations.dismiss();
        });
        queue.add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
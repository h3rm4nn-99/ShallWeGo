package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shallwego.client.databinding.ActivityStopReportBinding;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class StopReportActivity extends AppCompatActivity {

    private ActivityStopReportBinding binding;
    private ProgressDialog progressDialog;
    private MaterialTextView destinationsTextView;
    private double latitude, longitude;
    private TextInputLayout layoutCompany, layoutLine;
    private LinearLayout linesContainer;
    private LinearLayout checkBoxContainer;
    private LineResultItem resultItem = new LineResultItem();
    private final Set<LineResultItem> outputLines = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        latitude = i.getDoubleExtra("latitude", 0.0d);
        longitude = i.getDoubleExtra("longitude", 0.0d);
        binding = ActivityStopReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle("Nuova Fermata");

        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(v -> {
            submitData();
        });

        setLocation(latitude, longitude);
        linesContainer = findViewById(R.id.linesContainer);
        Chip addLine = findViewById(R.id.addLine);
        addLine.setOnClickListener((view) -> {
            prepareDialog();
        });
    }

    private void setLocation(double latitude, double longitude) {
        TextInputLayout layoutLocation = findViewById(R.id.location);
        layoutLocation.getEditText().setText("" + latitude + ", " + longitude);
    }

    private void prepareDialog() {
        progressDialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);
        View alertDialog = getLayoutInflater().inflate(R.layout.alert_dialog_add_lines_to_stop_report, null);
        layoutCompany = alertDialog.findViewById(R.id.company);
        layoutLine = alertDialog.findViewById(R.id.line);
        destinationsTextView = alertDialog.findViewById(R.id.destinationsText);
        AutoCompleteTextView editTextCompany = (AutoCompleteTextView) layoutCompany.getEditText();
        AutoCompleteTextView editTextLine = (AutoCompleteTextView) layoutLine.getEditText();
        checkBoxContainer = alertDialog.findViewById(R.id.checkboxContainer);

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getCompanies", response -> {
            JsonArray companiesJson = (JsonArray) JsonParser.parseString(response);
            Iterator<JsonElement> iterator = companiesJson.iterator();
            ArrayList<String> companies = new ArrayList<>();
            while (iterator.hasNext()) {
                companies.add(iterator.next().toString().replace("\"", ""));
            }
            Collections.sort(companies);
            editTextCompany.setAdapter(new ArrayAdapter<String>(StopReportActivity.this, R.layout.provincia_listitem_layout, companies));
            progressDialog.dismiss();
            populateDropDownMenus(editTextCompany, editTextLine);

            AlertDialog alertDialogObject = new MaterialAlertDialogBuilder(this).setView(alertDialog).setPositiveButton("Fatto", null).setTitle("Aggiungi linea").setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss()).show();
            alertDialogObject.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (checkFields()) {
                        resultItem.setCompanyName(editTextCompany.getText().toString());
                        resultItem.setLineIdentifier(editTextLine.getText().toString());
                        updateLineList(resultItem);
                        resultItem = new LineResultItem();
                        alertDialogObject.dismiss();
                    }
            });

        }, error -> {
            Toast.makeText(StopReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogCompanies = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog, which) -> StopReportActivity.this.finish()).show();
            alertDialogCompanies.setCancelable(false);
            alertDialogCompanies.setCanceledOnTouchOutside(false);
        });

        q.add(request);
    }

    private void populateDropDownMenus(AutoCompleteTextView editTextCompany, AutoCompleteTextView editTextLine) {
        editTextLine.setEnabled(false);
        editTextCompany.setOnItemClickListener((parent, view, position, id) -> {
            ProgressDialog dialogLines = ProgressDialog.show(StopReportActivity.this, "",
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
                editTextLine.setAdapter(new ArrayAdapter<String>(StopReportActivity.this, R.layout.provincia_listitem_layout, lines));
                dialogLines.dismiss();
                editTextLine.setOnItemClickListener((parent1, view1, position1, id1) -> {
                    processDestinations((String) parent1.getItemAtPosition(position1), editTextCompany.getText().toString());
                });
            }, error -> {
                Toast.makeText(StopReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                AlertDialog alertDialogLines = new AlertDialog.Builder(StopReportActivity.this)
                        .setMessage("Controlla la tua connessione ad Internet e riprova!")
                        .setPositiveButton("Ho capito!", (dialog1, which) -> StopReportActivity.this.finish()).show();
                alertDialogLines.setCancelable(false);
                alertDialogLines.setCanceledOnTouchOutside(false);
                dialogLines.dismiss();

            });
            q1.add(request1);

        });
    }

    public void processDestinations(String selectedLine, String selectedCompany) {
        ProgressDialog dialogDestinations = ProgressDialog.show(StopReportActivity.this, "",
                "Attendere prego...", true);
        destinationsTextView.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getLineDestinations/" + selectedCompany.replace(" ", "%20") + "/" + selectedLine.replace(" ", "%20"), (response) -> {
            JsonArray responseJsonArray = (JsonArray) JsonParser.parseString(response);
            ArrayList<String> destinations = new ArrayList<>();
            for (JsonElement destination: responseJsonArray) {
                destinations.add(destination.toString().replace("\"", ""));
            }
            checkBoxContainer.removeAllViews();
            int i = 0;
            for (String destination: destinations) {
                CheckBox currentDestination = new CheckBox(StopReportActivity.this);
                currentDestination.setText(destination);
                currentDestination.setOnCheckedChangeListener(listener);
                checkBoxContainer.addView(currentDestination, i++);
            }

            dialogDestinations.dismiss();
        }, (error) -> {
            Toast.makeText(StopReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogDestinations = new AlertDialog.Builder(StopReportActivity.this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> StopReportActivity.this.finish()).show();
            alertDialogDestinations.setCancelable(false);
            alertDialogDestinations.setCanceledOnTouchOutside(false);
            dialogDestinations.dismiss();
        });
        queue.add(request);
    }

    private boolean checkFields() {
        layoutCompany.setError(null);
        layoutLine.setError(null);
        destinationsTextView.setText("Destinazioni raggiungibili con questa linea da questa fermata");
        destinationsTextView.setTextColor(Color.WHITE);
        boolean check = true;
        if (layoutCompany.getEditText().getText().toString().equals("")) {
            check = false;
            layoutCompany.setError("Seleziona un'azienda.");
        }

        if (layoutLine.getEditText().getText().toString().equals("")) {
            check = false;
            layoutLine.setError("Seleziona una linea");
        }

        if (resultItem.getDestinations().isEmpty()) {
            check = false;
            destinationsTextView.setText("Seleziona almeno una destinazione tra quelle mostrate");
            destinationsTextView.setTextColor(Color.RED);
        }
        return check;
    }

    private void updateLineList(LineResultItem lineResultItem) {
        outputLines.add(new LineResultItem(lineResultItem));
        boolean check = false;
        for (int i = 0; i < linesContainer.getChildCount(); i++) {
            if (linesContainer.getChildAt(i).getTag() != null && linesContainer.getChildAt(i).getTag().toString().equalsIgnoreCase(resultItem.getCompanyName())) {
                ChipGroup chipGroup = linesContainer.getChildAt(i).findViewById(R.id.linesChipGroup);
                for (int j = 0; j < chipGroup.getChildCount(); j++) {
                    if (outputLines.contains(resultItem)) {
                        Toast.makeText(this, "Linea già aggiunta!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Chip chip = new Chip(this);
                String destinations = resultItem.getLineIdentifier() + " ";
                for (String destination: resultItem.getDestinations()) {
                    destinations += (destination + "/ ");
                }
                chip.setText(destinations.substring(0, destinations.length() - 2));
                chip.setTag(resultItem.getLineIdentifier());
                chip.setOnClickListener(v1 -> {
                    outputLines.remove(new LineResultItem(v1.getTag().toString(), ((LinearLayout) v1.getParent().getParent()).getTag().toString()));
                    chipGroup.removeView(v1);
                    if (chipGroup.getChildCount() == 0) {
                        View viewParent = (View) chipGroup.getParent();
                        LinearLayout rootLayout = (LinearLayout) viewParent.getParent();
                        rootLayout.removeView(viewParent);
                    }
                });
                chipGroup.addView(chip);
                check = true;
                break;
            }
        }

        if (!check) {
            View v = getLayoutInflater().inflate(R.layout.stop_details_company_lines_layout, null);
            v.setTag(resultItem.getCompanyName());
            MaterialTextView companyName = v.findViewById(R.id.companyName);
            companyName.setText(resultItem.getCompanyName());
            ChipGroup chipGroup = v.findViewById(R.id.linesChipGroup);
            Chip chip = new Chip(this);
            chip.setTag(resultItem.getLineIdentifier());
            chip.setOnClickListener(v1 -> {
                outputLines.remove(new LineResultItem(v1.getTag().toString(), ((LinearLayout) v1.getParent().getParent()).getTag().toString()));
                chipGroup.removeView(v1);
                if (chipGroup.getChildCount() == 0) {
                    View viewParent = (View) chipGroup.getParent();
                    LinearLayout rootLayout = (LinearLayout) viewParent.getParent();
                    rootLayout.removeView(viewParent);
                }
            });
            String destinations = resultItem.getLineIdentifier() + " ";
            for (String destination: resultItem.getDestinations()) {
                destinations += (destination + "/ ");
            }
            chip.setText(destinations.substring(0, destinations.length() - 2));
            chipGroup.addView(chip);
            linesContainer.addView(v, linesContainer.getChildCount());
        }
    }

    CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
        if (isChecked) {
            resultItem.getDestinations().add(buttonView.getText().toString());
        } else {
            resultItem.getDestinations().remove(buttonView.getText().toString());
        }

    };

    private void submitData() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        JsonObject object = new JsonObject();

        boolean check = true;
        TextInputLayout stopName = findViewById(R.id.stopName);
        RadioGroup timetablesRadioGroup = findViewById(R.id.radioGroupQuadriOrari);
        RadioGroup shelterRadioGroup = findViewById(R.id.radioGroupPensilina);
        RadioGroup stopSignRadioGroup = findViewById(R.id.radioGroupPalina);
        MaterialCardView timetablesCardView = findViewById(R.id.cardViewQuadriOrari);
        MaterialCardView shelterCardView = findViewById(R.id.cardViewPensilina);
        MaterialCardView stopSignCardView = findViewById(R.id.cardViewPalinaAziendale);

        stopName.setError(null);
        timetablesCardView.setCardBackgroundColor(Color.parseColor("#6200EE"));
        shelterCardView.setCardBackgroundColor(Color.parseColor("#6200EE"));
        stopSignCardView.setCardBackgroundColor(Color.parseColor("#6200EE"));


        if (timetablesRadioGroup.getCheckedRadioButtonId() == -1) {
            timetablesCardView.setCardBackgroundColor(Color.RED);
            check = false;
        }

        if (shelterRadioGroup.getCheckedRadioButtonId() == -1) {
            shelterCardView.setCardBackgroundColor(Color.RED);
            check = false;
        }

        if (stopSignRadioGroup.getCheckedRadioButtonId() == -1) {
            stopSignCardView.setCardBackgroundColor(Color.RED);
            check = false;
        }

        if (stopName.getEditText().getText().toString().equals("")) {
            stopName.setError("Inserisci il nome della fermata!");
            check = false;
        }

        if (!check) {
            return;
        }

        boolean hasShelter = ((MaterialRadioButton) findViewById(R.id.shelterYes)).isChecked();
        boolean hasTimeTables = ((MaterialRadioButton) findViewById(R.id.timeTablesYes)).isChecked();
        boolean hasStopSign = ((MaterialRadioButton) findViewById(R.id.stopSignYes)).isChecked();

        object.addProperty("stopName", stopName.getEditText().getText().toString());
        object.addProperty("latitude", latitude);
        object.addProperty("longitude", longitude);
        object.addProperty("userName", MainActivity.userName);
        object.addProperty("hasShelter", hasShelter);
        object.addProperty("hasStopSign", hasStopSign);
        object.addProperty("hasTimeTables", hasTimeTables);
        object.addProperty("date", formatter.format(new Date()));
        JsonArray lines = new JsonArray();
        for (LineResultItem item: outputLines) {
            JsonObject lineObject = new JsonObject();
            lineObject.addProperty("lineIdentifier", item.getLineIdentifier());
            lineObject.addProperty("companyName", item.getCompanyName());
            JsonArray destinationsArray = new JsonArray();
            for (String destination: item.getDestinations()) {
                destinationsArray.add(destination);
            }
            lineObject.add("destinations", destinationsArray);
            lines.add(lineObject);
        }
        object.add("lines", lines);

        progressDialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);
        String requestBody = object.toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, IpAddress.SERVER_IP_ADDRESS + "/api/newStopReport/" + MainActivity.userName, (response) -> {
            JsonArray verifiers = (JsonArray) JsonParser.parseString(response);
            ArrayList<String> verifiersArray = new ArrayList<>();
            for (JsonElement element: verifiers) {
                verifiersArray.add(element.getAsString());
            }
            View v = getLayoutInflater().inflate(R.layout.alert_dialog_after_report, null);
            ListView listView = v.findViewById(R.id.listViewVerifiers);
            AlertDialog.Builder verifiersDialog = new AlertDialog.Builder(this).setView(v);
            listView.setAdapter(new ArrayAdapter<String>(StopReportActivity.this, R.layout.provincia_listitem_layout, verifiersArray));
            verifiersDialog.setTitle("Ecco la zona di chi verificherà la tua segnalazione");
            verifiersDialog.setPositiveButton("Capito!", ((dialog, which) -> {
                dialog.dismiss();
                StopReportActivity.this.finish();
            }));
            progressDialog.dismiss();
            verifiersDialog.show();
        }, (error) -> {
            Toast.makeText(StopReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(StopReportActivity.this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> StopReportActivity.this.finish()).show();
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

    protected static class LineResultItem implements Serializable {
        private String lineIdentifier, companyName;
        private List<String> destinations = new ArrayList<>();

        public LineResultItem(String lineIdentifier, String companyName, List<String> destinations) {
            this.lineIdentifier = lineIdentifier;
            this.companyName = companyName;
            this.destinations = destinations;
        }

        public LineResultItem(String lineIdentifier, String companyName) {
            this.lineIdentifier = lineIdentifier;
            this.companyName = companyName;
        }

        public LineResultItem(LineResultItem item) {
            this.lineIdentifier = item.getLineIdentifier();
            this.companyName = item.getCompanyName();
            this.destinations = item.getDestinations();
        }

        public LineResultItem() {

        }

        public String getLineIdentifier() {
            return lineIdentifier;
        }

        public void setLineIdentifier(String lineIdentifier) {
            this.lineIdentifier = lineIdentifier;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public List<String> getDestinations() {
            return destinations;
        }

        public void setDestinations(List<String> destinations) {
            this.destinations = destinations;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LineResultItem)) return false;
            LineResultItem that = (LineResultItem) o;
            return Objects.equals(getLineIdentifier(), that.getLineIdentifier()) && Objects.equals(getCompanyName(), that.getCompanyName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getLineIdentifier(), getCompanyName());
        }

        @Override
        public String toString() {
            return "LineResultItem{" +
                    "lineIdentifier='" + lineIdentifier + '\'' +
                    ", companyName='" + companyName + '\'' +
                    ", destinations=" + destinations +
                    '}';
        }
    }
}
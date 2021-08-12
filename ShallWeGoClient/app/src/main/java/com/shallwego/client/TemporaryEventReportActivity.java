package com.shallwego.client;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shallwego.client.databinding.ActivityTemporaryEventReportBinding;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TemporaryEventReportActivity extends AppCompatActivity {

    private ActivityTemporaryEventReportBinding binding;
    private TextInputEditText startEditText, endEditText;
    private final static Integer END = 1;
    private final static Integer START = 0;
    private StopReportActivity.LineResultItem resultItem = new StopReportActivity.LineResultItem();
    private final Set<StopReportActivity.LineResultItem> linesAffected = new HashSet<>();
    private ProgressDialog progressDialog;
    private TextInputLayout layoutCompany, layoutLine, eventType, layoutDescription, layoutSource;
    private LinearLayout linesContainer;
    private LinearLayout checkBoxContainer;
    private MaterialTextView destinationsTextView;
    private CheckBox endCheckbox;
    private double latitude, longitude;

    private final String[] types = new String[] {
            "Incidente",
            "Traffico",
            "Deviazione"
    };
    private TextInputLayout layoutStart, layoutEnd;

    private final View.OnClickListener onClickDate = (v) -> {
        int tag = (int) v.getTag();
        MaterialDatePicker.Builder<Long> pickerBuilder = MaterialDatePicker.Builder.datePicker().setTitleText("Seleziona una data");
        MaterialDatePicker<Long> picker = pickerBuilder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Date currentDate = new Date();
            currentDate.setTime(selection);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String dateFormatted = formatter.format(currentDate);
            if (tag == START) {
                startEditText.setText(dateFormatted);
            } else {
                endEditText.setText(dateFormatted);
            }
        });
        picker.show(getSupportFragmentManager(), String.valueOf(tag));

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTemporaryEventReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle("Che succede?");

        FloatingActionButton fab = binding.fab;

        Intent i = getIntent();
        latitude = i.getDoubleExtra("latitude", 0d);
        longitude = i.getDoubleExtra("longitude", 0d);
        setLocation(latitude, longitude);
        layoutEnd = findViewById(R.id.dateEnd);
        endEditText = (TextInputEditText) layoutEnd.getEditText();
        endEditText.setTag(END);

        layoutStart = findViewById(R.id.dateStart);
        startEditText = (TextInputEditText) layoutStart.getEditText();
        startEditText.setTag(START);

        startEditText.setOnClickListener(onClickDate);
        endEditText.setOnClickListener(onClickDate);


        linesContainer = findViewById(R.id.linesContainer);

        endCheckbox = findViewById(R.id.endCheckBox);
        endCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                endEditText.setText("");
                layoutEnd.setVisibility(View.GONE);
            } else {
                endEditText.setText("");
                layoutEnd.setVisibility(View.VISIBLE);
            }
        });

        fab.setOnClickListener((v -> {
            try {
                submitData();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }));

        Chip addLine = findViewById(R.id.addAffectedLine);
        addLine.setOnClickListener((view) -> {
            prepareDialog();
        });

        eventType = findViewById(R.id.eventType);
        AutoCompleteTextView eventTypeEditText = (AutoCompleteTextView) eventType.getEditText();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.provincia_listitem_layout, Arrays.asList(types));
        eventTypeEditText.setAdapter(adapter);
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
            editTextCompany.setAdapter(new ArrayAdapter<String>(TemporaryEventReportActivity.this, R.layout.provincia_listitem_layout, companies));
            progressDialog.dismiss();
            populateDropDownMenus(editTextCompany, editTextLine);

            AlertDialog alertDialogObject = new MaterialAlertDialogBuilder(this).setView(alertDialog).setPositiveButton("Fatto", null).setTitle("Aggiungi linea").setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss()).show();
            alertDialogObject.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (checkFields()) {
                    resultItem.setCompanyName(editTextCompany.getText().toString());
                    resultItem.setLineIdentifier(editTextLine.getText().toString());
                    updateLineList(resultItem);
                    resultItem = new StopReportActivity.LineResultItem();
                    alertDialogObject.dismiss();
                }
            });

        }, error -> {
            Toast.makeText(TemporaryEventReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogCompanies = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog, which) -> TemporaryEventReportActivity.this.finish()).show();
            alertDialogCompanies.setCancelable(false);
            alertDialogCompanies.setCanceledOnTouchOutside(false);
        });

        q.add(request);
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

    private void populateDropDownMenus(AutoCompleteTextView editTextCompany, AutoCompleteTextView editTextLine) {
        editTextLine.setEnabled(false);
        editTextCompany.setOnItemClickListener((parent, view, position, id) -> {
            ProgressDialog dialogLines = ProgressDialog.show(TemporaryEventReportActivity.this, "",
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
                editTextLine.setAdapter(new ArrayAdapter<String>(TemporaryEventReportActivity.this, R.layout.provincia_listitem_layout, lines));
                dialogLines.dismiss();
                editTextLine.setOnItemClickListener((parent1, view1, position1, id1) -> {
                    processDestinations((String) parent1.getItemAtPosition(position1), editTextCompany.getText().toString());
                });
            }, error -> {
                Toast.makeText(TemporaryEventReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                AlertDialog alertDialogLines = new AlertDialog.Builder(TemporaryEventReportActivity.this)
                        .setMessage("Controlla la tua connessione ad Internet e riprova!")
                        .setPositiveButton("Ho capito!", (dialog1, which) -> TemporaryEventReportActivity.this.finish()).show();
                alertDialogLines.setCancelable(false);
                alertDialogLines.setCanceledOnTouchOutside(false);
                dialogLines.dismiss();

            });
            q1.add(request1);

        });
    }

    private void updateLineList(StopReportActivity.LineResultItem lineResultItem) {
        linesAffected.add(new StopReportActivity.LineResultItem(lineResultItem));
        boolean check = false;
        for (int i = 0; i < linesContainer.getChildCount(); i++) {
            if (linesContainer.getChildAt(i).getTag() != null && linesContainer.getChildAt(i).getTag().toString().equalsIgnoreCase(resultItem.getCompanyName())) {
                ChipGroup chipGroup = linesContainer.getChildAt(i).findViewById(R.id.linesChipGroup);
                for (int j = 0; j < chipGroup.getChildCount(); j++) {
                    if (linesAffected.contains(resultItem)) {
                        Toast.makeText(this, "Linea già aggiunta!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Chip chip = new Chip(this);
                String destinations = resultItem.getLineIdentifier() + " ";
                for (String destination : resultItem.getDestinations()) {
                    destinations += (destination + "/ ");
                }
                chip.setText(destinations.substring(0, destinations.length() - 2));
                chip.setTag(resultItem.getLineIdentifier());
                chip.setOnClickListener(v1 -> {
                    linesAffected.remove(new StopReportActivity.LineResultItem(v1.getTag().toString(), ((LinearLayout) v1.getParent().getParent()).getTag().toString()));
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
                linesAffected.remove(new StopReportActivity.LineResultItem(v1.getTag().toString(), ((LinearLayout) v1.getParent().getParent()).getTag().toString()));
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

    public void processDestinations(String selectedLine, String selectedCompany) {
        ProgressDialog dialogDestinations = ProgressDialog.show(TemporaryEventReportActivity.this, "",
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
                CheckBox currentDestination = new CheckBox(TemporaryEventReportActivity.this);
                currentDestination.setText(destination);
                currentDestination.setOnCheckedChangeListener(listener);
                checkBoxContainer.addView(currentDestination, i++);
            }

            dialogDestinations.dismiss();
        }, (error) -> {
            Toast.makeText(TemporaryEventReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogDestinations = new AlertDialog.Builder(TemporaryEventReportActivity.this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> TemporaryEventReportActivity.this.finish()).show();
            alertDialogDestinations.setCancelable(false);
            alertDialogDestinations.setCanceledOnTouchOutside(false);
            dialogDestinations.dismiss();
        });
        queue.add(request);
    }

    private void submitData() throws ParseException {
        if (!validateData()) {
            return;
        } else {
            JsonObject output = new JsonObject();
            output.addProperty("latitude", latitude);
            output.addProperty("longitude", longitude);
            output.addProperty("date", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            output.addProperty("description", layoutDescription.getEditText().getText().toString());
            output.addProperty("source", layoutSource.getEditText().getText().toString());
            output.addProperty("type", eventType.getEditText().getText().toString());
            output.addProperty("start", startEditText.getText().toString());
            if (endCheckbox.isChecked()) {
                output.addProperty("end", endEditText.getText().toString());
            }

            JsonArray lines = new JsonArray();
            for (StopReportActivity.LineResultItem item: linesAffected) {
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
            output.add("linesAffected", lines);

            progressDialog = ProgressDialog.show(this, "",
                    "Attendere prego...", true);
            String requestBody = output.toString();
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.PUT, IpAddress.SERVER_IP_ADDRESS + "/api/newTemporaryEventReport/" + MainActivity.userName, (response) -> {

                AlertDialog.Builder verifiersDialog = new AlertDialog.Builder(this);
                verifiersDialog.setTitle("Tutto fatto!");
                verifiersDialog.setPositiveButton("Capito!", ((dialog, which) -> {
                    dialog.dismiss();
                    TemporaryEventReportActivity.this.finish();
                }));
                progressDialog.dismiss();
                verifiersDialog.show();
            }, (error) -> {
                Toast.makeText(TemporaryEventReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                AlertDialog alertDialog = new AlertDialog.Builder(TemporaryEventReportActivity.this)
                        .setMessage("Controlla la tua connessione ad Internet e riprova!")
                        .setPositiveButton("Ho capito!", (dialog1, which) -> TemporaryEventReportActivity.this.finish()).show();
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

    private boolean validateData() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        boolean check = true;

        if (eventType.getEditText().getText().toString().isEmpty()) {
            eventType.setError("Seleziona una tipologia di evento!");
            check = false;
        }

        Date start = null, end = null;
        if (startEditText.getText().toString().isEmpty()) {
            layoutStart.setError("Seleziona una data di inizio!");
            check = false;
        } else {
            start = formatter.parse(startEditText.getText().toString());
        }

        if (endEditText.getText().toString().isEmpty() && endCheckbox.isChecked()) {
            layoutEnd.setError("Seleziona una data di fine!");
            check = false;
        } else {
            if (endCheckbox.isChecked()) {
                end = formatter.parse(endEditText.getText().toString());
            }
        }

        layoutDescription = findViewById(R.id.description);
        layoutSource = findViewById(R.id.source);

        if (layoutDescription.getEditText().getText().toString().isEmpty()) {
            layoutDescription.setError("Dicci qualcosa di più!");
            check = false;
        }

        if (layoutSource.getEditText().getText().toString().isEmpty()) {
            layoutSource.setError("Da dove hai preso questa informazione?");
            check = false;
        }

        if (start != null && end != null && endCheckbox.isChecked() && end.before(start)) {
           layoutStart.setError("I viaggi nel tempo non sono ancora supportati :)");
           check = false;
        }

        return check;
    }
}
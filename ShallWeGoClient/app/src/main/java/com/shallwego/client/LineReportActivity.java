package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.*;
import com.shallwego.client.databinding.ActivityLineReportBinding;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class LineReportActivity extends AppCompatActivity {

    private ActivityLineReportBinding binding;
    private TextInputLayout companyLayout, provinciaOriginLayout, provinciaDestinationLayout, comuneOriginLayout, comuneDestinationLayout, furtherDetailsOrigin, furtherDetailsDestination;
    private ArrayList<String> companies;
    private final HashMap<String, List<String>> comuni = new HashMap<>();
    private ArrayList<String> province;
    private final Set<Destination> otherDestinations = new HashSet<>();
    private ChipGroup otherDestinationsChipGroup;
    private TextInputEditText identifierEditText;
    private MaterialCheckBox hasIdentifier;
    private ImageView moreInfoIdentifier, moreInfoOrigin, moreInfoDestinations;
    private final View.OnClickListener hideKeyboard = v -> { //necessario per evitare che gli elementi della lista dropdown vengano nascosti dalla tastiera
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLineReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle("Nuova Linea");

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener((view) -> {
            validateInput();
        });
        identifierEditText = findViewById(R.id.editTextIdentifier);
        hasIdentifier = findViewById(R.id.hasIdentifier);
        setUpIdentifierField();
        setUpCompanyDropDown();

        Chip chip = findViewById(R.id.anotherDestination);
        comuneOriginLayout = findViewById(R.id.comuneOrigin);
        comuneDestinationLayout = findViewById(R.id.comuneDestination);
        furtherDetailsOrigin = findViewById(R.id.furtherDetailsOrigin);
        furtherDetailsDestination = findViewById(R.id.furtherDetailsDestination);
        otherDestinationsChipGroup = findViewById(R.id.chipGroupOtherDestinations);
        moreInfoOrigin = findViewById(R.id.moreInfoOrigin);
        moreInfoDestinations = findViewById(R.id.moreInfoDestination);
        moreInfoIdentifier = findViewById(R.id.moreInfoIdentifier);

        moreInfoIdentifier.setOnClickListener(moreInfo);
        moreInfoDestinations.setOnClickListener(moreInfo);
        moreInfoOrigin.setOnClickListener(moreInfo);


        chip.setOnClickListener((view) -> showAnotherDestinationDialogBox());
    }

    View.OnClickListener moreInfo = v -> {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this).setTitle("Per saperne di più");

        String which = (String) v.getTag();
        switch (which) {
            case "identifier": {
                dialogBuilder.setMessage(R.string.help_identifier);
                break;
            }

            case "origin": {
                dialogBuilder.setMessage(R.string.help_origin);
                break;
            }

            case "destination": {
                dialogBuilder.setMessage(R.string.help_destination);
                break;
            }
        }

        dialogBuilder.setPositiveButton("Ricevuto!", (dialog, which1) -> {
            dialog.dismiss();
        }).show();
    };

    private void validateInput() {
        boolean check = true;
        provinciaOriginLayout.setError(null);
        provinciaDestinationLayout.setError(null);
        comuneOriginLayout.setError(null);
        comuneDestinationLayout.setError(null);
        furtherDetailsOrigin.setError(null);
        furtherDetailsDestination.setError(null);

        String provinciaOrigin = provinciaOriginLayout.getEditText().getText().toString();
        String provinciaDestination = provinciaDestinationLayout.getEditText().getText().toString();
        String comuneOrigin = comuneOriginLayout.getEditText().getText().toString();
        String comuneDestination = comuneDestinationLayout.getEditText().getText().toString();
        String detailsOrigin = furtherDetailsOrigin.getEditText().getText().toString();
        String detailsDestination = furtherDetailsDestination.getEditText().getText().toString();

        TextInputLayout company = findViewById(R.id.company);
        if (company.getEditText().getText().toString().isEmpty()) {
            company.setError("Inserisci l'azienda");
            check = false;
        }

        if (provinciaOrigin.isEmpty()) {
            provinciaOriginLayout.setError("Seleziona la provincia");
            check = false;
        }

        if (provinciaDestination.isEmpty()) {
            provinciaDestinationLayout.setError("Seleziona la provincia");
            check = false;
        }

        if (comuneOrigin.isEmpty()) {
            comuneOriginLayout.setError("Seleziona il comune");
            check = false;
        }

        if (comuneDestination.isEmpty()) {
            comuneDestinationLayout.setError("Seleziona il comune");
            check = false;
        }

        if (comuneOrigin.equals(comuneDestination) && (detailsOrigin.isEmpty() || detailsDestination.isEmpty())) {
            furtherDetailsOrigin.setError("Aggiungi un nome all'origine per permettere di disambiguare!");
            furtherDetailsDestination.setError("Aggiungi un nome alla destinazione per permettere di disambiguare");
            check = false;
        }

        if (comuneOrigin.equals(comuneDestination) && detailsOrigin.equals(detailsDestination)) {
            furtherDetailsOrigin.setError("L'origine deve essere diversa dalla destinazione!");
            furtherDetailsDestination.setError("L'origine deve essere diversa dalla destinazione!");
            check = false;
        }

        if (hasIdentifier.isChecked()) {
            if (identifierEditText.getText().toString().isEmpty()) {
                ((TextInputLayout) identifierEditText.getParent().getParent()).setError("Inserisci un identificativo.");
                check = false;
            }
        }

        if (check) {
            submitData(provinciaOrigin, provinciaDestination, comuneOrigin, comuneDestination, detailsOrigin, detailsDestination);
        }
    }

    private void submitData(String provinciaOrigin, String provinciaDestination, String comuneOrigin, String comuneDestination, String detailsOrigin, String detailsDestination) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        JsonObject dataToBeSent = new JsonObject();
        dataToBeSent.addProperty("company", companyLayout.getEditText().getText().toString());
        dataToBeSent.addProperty("hasIdentifier", hasIdentifier.isChecked());
        dataToBeSent.addProperty("comuneOrigin", comuneOrigin);
        dataToBeSent.addProperty("detailsOrigin", detailsOrigin);
        dataToBeSent.addProperty("comuneDestination", comuneDestination);
        dataToBeSent.addProperty("detailsDestination", detailsDestination);
        if (hasIdentifier.isChecked()) {
            dataToBeSent.addProperty("identifier", identifierEditText.getText().toString());
        }
        Set<Destination> destinations = new HashSet<>();
        Destination origin = new Destination(comuneOrigin, provinciaOrigin, detailsOrigin);
        Destination destination = new Destination(comuneDestination, provinciaDestination, detailsDestination);

        destinations.add(origin);
        destinations.add(destination);
        destinations.addAll(otherDestinations);

        JsonArray destinationsJson = new JsonArray();
        Gson gson = new Gson();

        for (Destination currentDestination: destinations) {
            destinationsJson.add(gson.toJsonTree(currentDestination));
        }
        dataToBeSent.add("destinations", destinationsJson);
        dataToBeSent.addProperty("date", formatter.format(new Date()));
        dataToBeSent.addProperty("userName", MainActivity.userName);

        String requestBody = dataToBeSent.toString();
        ProgressDialog progressDialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, IpAddress.SERVER_IP_ADDRESS + "/api/newLineReport/" + MainActivity.userName, (response) -> {
            JsonArray verifiers = (JsonArray) JsonParser.parseString(response);
            ArrayList<String> verifiersArray = new ArrayList<>();
            for (JsonElement element: verifiers) {
                verifiersArray.add(element.getAsString());
            }
            View v = getLayoutInflater().inflate(R.layout.alert_dialog_after_report, null);
            ListView listView = v.findViewById(R.id.listViewVerifiers);
            AlertDialog.Builder verifiersDialog = new AlertDialog.Builder(this).setView(v);
            listView.setAdapter(new ArrayAdapter<String>(LineReportActivity.this, R.layout.provincia_listitem_layout, verifiersArray));
            verifiersDialog.setTitle("Ecco la zona di chi verificherà la tua segnalazione");
            verifiersDialog.setPositiveButton("Capito!", ((dialog, which) -> {
                dialog.dismiss();
                LineReportActivity.this.finish();
            }));
            progressDialog.dismiss();
            verifiersDialog.show();
        }, (error) -> {
            Toast.makeText(LineReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialog = new AlertDialog.Builder(LineReportActivity.this)
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

    private void setUpIdentifierField() {
        hasIdentifier.setOnCheckedChangeListener((buttonView, isChecked) -> {
            identifierEditText.setText("");
            identifierEditText.setEnabled(isChecked);
        });

    }

    private void showAnotherDestinationDialogBox() {
        View view = getLayoutInflater().inflate(R.layout.another_destination_line_report, null);
        TextInputLayout anotherProvincia = view.findViewById(R.id.provinciaAnotherDestination);
        TextInputLayout anotherComune = view.findViewById(R.id.comuneAnotherDestination);
        TextInputLayout anotherName = view.findViewById(R.id.furtherDetailsAnotherDestination);

        AutoCompleteTextView anotherProvinciaTextView = (AutoCompleteTextView) anotherProvincia.getEditText();
        AutoCompleteTextView anotherComuneTextView = (AutoCompleteTextView) anotherComune.getEditText();
        TextInputEditText anotherNameEditText = (TextInputEditText) anotherName.getEditText();
        anotherProvinciaTextView.setAdapter(new ArrayAdapter<String>(LineReportActivity.this, R.layout.provincia_listitem_layout, province));
        anotherProvinciaTextView.setOnItemClickListener((parent, view1, position, id) -> {
            populateComuniEditText(anotherComune, (String) parent.getItemAtPosition(position));
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(LineReportActivity.this).setView(view).setTitle("Aggiungi un'altra destinazione");
        builder.setPositiveButton("Fatto", null);
        builder.setNegativeButton("Annulla", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(((view1) -> {
            if (checkAnotherDestination(anotherProvinciaTextView, anotherComuneTextView, anotherNameEditText)) {
                dialog.dismiss();
            }
        }));
    }

    private boolean checkAnotherDestination(AutoCompleteTextView anotherProvinciaTextView, AutoCompleteTextView anotherComuneTextView, TextInputEditText anotherNameEditText) {
        ((TextInputLayout) anotherProvinciaTextView.getParent().getParent()).setError(null);
        ((TextInputLayout) anotherComuneTextView.getParent().getParent()).setError(null);
        ((TextInputLayout) anotherNameEditText.getParent().getParent()).setError(null);
        boolean check = true;

        if (anotherProvinciaTextView.getText().toString().isEmpty()) {
            check = false;
            ((TextInputLayout) anotherProvinciaTextView.getParent().getParent()).setError("Scegli una provincia");
        }

        if (anotherComuneTextView.getText().toString().isEmpty()) {
            check = false;
            ((TextInputLayout) anotherComuneTextView.getParent().getParent()).setError("Scegli un comune");
        }
        
        if (anotherNameEditText.getText().toString().isEmpty()) {
            check = false;
            ((TextInputLayout) anotherNameEditText.getParent().getParent()).setError("Inserisci il nome della destinazione");
        } else {
            switch (disambiguate(anotherComuneTextView.getText().toString(), anotherNameEditText.getText().toString())) {
                case LineReportCheckConstants.DESTINATION_ALREADY_PRESENT: {
                    check = false;
                    ((TextInputLayout) anotherNameEditText.getParent().getParent()).setError("È già presente una destinazione con questo comune e questa destinazione!");
                    break;
                } case LineReportCheckConstants.DISAMBIGUATE_ORIGIN: {
                    check = false;
                    ((TextInputLayout) anotherNameEditText.getParent().getParent()).setError("Hai inserito lo stesso comune dell'origine ma quest'ultima non ha un nome specificato. Aggiungilo lì e riprova.");
                    break;
                } case LineReportCheckConstants.DISAMBIGUATE_DESTINATION: {
                    check = false;
                    ((TextInputLayout) anotherNameEditText.getParent().getParent()).setError("Hai inserito lo stesso comune della destinazione ma quest'ultima non ha un nome specificato. Aggiungilo lì e riprova.");
                    break;
                } default: {
                    check = true;
                }
            }
        }
        
        if (!check) {
            return false;
        }
        else {
            Destination destination = new Destination(anotherComuneTextView.getText().toString(), anotherProvinciaTextView.getText().toString(), anotherNameEditText.getText().toString());
            otherDestinations.add(destination);
            drawChip(destination);
            return true;
        }
    }

    private void drawChip(Destination destination) {
        Chip chip = new Chip(LineReportActivity.this);
        chip.setOnClickListener((v1) -> {
            otherDestinationsChipGroup.removeView(v1);
            otherDestinations.remove(destination);
        });
        chip.setText(destination.comune + " " + destination.name);
        otherDestinationsChipGroup.addView(chip);
    }

    private int disambiguate(String comune, String destinationName) {
        
        String comuneOrigin = comuneOriginLayout.getEditText().getText().toString();
        String comuneDestination = comuneDestinationLayout.getEditText().getText().toString();
        String nameOrigin = furtherDetailsOrigin.getEditText().getText().toString();
        String nameDestination = furtherDetailsDestination.getEditText().getText().toString();

        for (Destination destination : otherDestinations) {
            if (destination.getComune().equals(comune) && destination.getName().equals(destinationName)) {
                return LineReportCheckConstants.DESTINATION_ALREADY_PRESENT;
            }
        }
            
        if (comuneOrigin.equals(comune) && nameOrigin.isEmpty()) {
            return LineReportCheckConstants.DISAMBIGUATE_ORIGIN;
        }

        if (comuneDestination.equals(comune) && nameDestination.isEmpty()) {
            return LineReportCheckConstants.DISAMBIGUATE_DESTINATION;
        }
            
        if (comuneOrigin.equals(comune) && nameOrigin.trim().toLowerCase().equalsIgnoreCase(destinationName.trim().toLowerCase())) {
            return LineReportCheckConstants.DESTINATION_ALREADY_PRESENT;
        }
        
        if (comuneDestination.equals(comune) && nameDestination.trim().toLowerCase().equalsIgnoreCase(destinationName.trim().toLowerCase())) {
            return LineReportCheckConstants.DESTINATION_ALREADY_PRESENT;
        }
        
        return LineReportCheckConstants.CHECK_OKAY;
    }

    private void setUpProvinciaDropDown() {
        provinciaOriginLayout = findViewById(R.id.provinciaOrigin);
        provinciaDestinationLayout = findViewById(R.id.provinciaDestination);
        AutoCompleteTextView provinciaOriginEditText = (AutoCompleteTextView) provinciaOriginLayout.getEditText();
        provinciaOriginEditText.setOnClickListener(hideKeyboard);

        AutoCompleteTextView provinciaDestinationEditText = (AutoCompleteTextView) provinciaDestinationLayout.getEditText();
        provinciaDestinationEditText.setOnClickListener(hideKeyboard);
        addOnItemClickListener(provinciaOriginEditText, provinciaDestinationEditText);
        ProgressDialog progressDialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);
        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/province", response -> {
            progressDialog.dismiss();
            JsonArray provinceJson = (JsonArray) JsonParser.parseString(response);
            Iterator<JsonElement> iterator = provinceJson.iterator();
            province = new ArrayList<>();
            while (iterator.hasNext()) {
                province.add(iterator.next().toString().replace("\"", ""));
            }

            Collections.sort(province);
            provinciaOriginEditText.setAdapter(new ArrayAdapter<String>(LineReportActivity.this, R.layout.provincia_listitem_layout, province));
            provinciaDestinationEditText.setAdapter(new ArrayAdapter<String>(LineReportActivity.this, R.layout.provincia_listitem_layout, province));

        }, error -> {
            progressDialog.dismiss();
            Toast.makeText(LineReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogProvince = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Capito", null)
                    .show();
            alertDialogProvince.setCancelable(false);
            alertDialogProvince.setCanceledOnTouchOutside(false);
            alertDialogProvince.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialogProvince.dismiss();
                    LineReportActivity.this.finish();
                }
            });
        });

        q.add(request);
    }

    private void addOnItemClickListener(AutoCompleteTextView provinciaOriginEditText, AutoCompleteTextView provinciaDestinationEditText) {
        AdapterView.OnItemClickListener listener = (parent, view, position, id) -> {
            Adapter adapter = parent.getAdapter();
            if (adapter == provinciaOriginEditText.getAdapter()) {
                populateComuniEditText(comuneOriginLayout, (String) parent.getItemAtPosition(position));
            } else {
                populateComuniEditText(comuneDestinationLayout, (String) parent.getItemAtPosition(position));
            }
        };
        provinciaOriginEditText.setOnItemClickListener(listener);
        provinciaDestinationEditText.setOnItemClickListener(listener);
    }

    private void populateComuniEditText(TextInputLayout comuneLayout, String provincia) {
        ProgressDialog dialogComuni = ProgressDialog.show(LineReportActivity.this, "",
                "Attendere prego...", true);
        AutoCompleteTextView editTextComune = (AutoCompleteTextView) comuneLayout.getEditText();
        editTextComune.setText("");

        if (comuni.get(provincia) != null) {
            editTextComune.setAdapter(new ArrayAdapter<String>(LineReportActivity.this, R.layout.provincia_listitem_layout, comuni.get(provincia)));
            dialogComuni.dismiss();
            return;
        }
        RequestQueue q1 = Volley.newRequestQueue(getApplicationContext());
        StringRequest request1 = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/provincia/" + provincia + "/comuni", response -> {
            JsonArray comuniJson = (JsonArray) JsonParser.parseString(response);
            Iterator<JsonElement> iterator = comuniJson.iterator();
            ArrayList<String> comuniList = new ArrayList<>();
            while (iterator.hasNext()) {
                comuniList.add(iterator.next().toString().replace("\"", ""));
            }
            Collections.sort(comuniList);
            editTextComune.setAdapter(new ArrayAdapter<String>(LineReportActivity.this, R.layout.provincia_listitem_layout, comuniList));
            comuni.put(provincia, comuniList);
            dialogComuni.dismiss();

        }, error -> {
            Toast.makeText(LineReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogComuni = new AlertDialog.Builder(LineReportActivity.this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog1, which) -> LineReportActivity.this.finish()).show();
            alertDialogComuni.setCancelable(false);
            alertDialogComuni.setCanceledOnTouchOutside(false);
            dialogComuni.dismiss();

        });
        q1.add(request1);
    }

    private void setUpCompanyDropDown() {
        companyLayout = findViewById(R.id.company);
        AutoCompleteTextView editTextCompany = (AutoCompleteTextView) companyLayout.getEditText();
        ProgressDialog progressDialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);
        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/getCompanies", response -> {
            progressDialog.dismiss();
            JsonArray companiesJson = (JsonArray) JsonParser.parseString(response);
            Iterator<JsonElement> iterator = companiesJson.iterator();
            companies = new ArrayList<>();
            while (iterator.hasNext()) {
                companies.add(iterator.next().toString().replace("\"", ""));
            }
            Collections.sort(companies);
            editTextCompany.setAdapter(new ArrayAdapter<String>(LineReportActivity.this, R.layout.provincia_listitem_layout, companies));
            setUpProvinciaDropDown();
        }, error -> {
            progressDialog.dismiss();
            Toast.makeText(LineReportActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogCompanies = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog, which) -> {
                        dialog.dismiss();
                        LineReportActivity.this.finish();
                    }).show();
            alertDialogCompanies.setCancelable(false);
            alertDialogCompanies.setCanceledOnTouchOutside(false);
        });

        q.add(request);
    }

    private class Destination implements Serializable {
        private String comune;
        private String provincia;
        private String name;

        private Destination(String comune, String provincia, String name) {
            this.comune = comune;
            this.provincia = provincia;
            this.name = name;
        }

        private Destination() {}

        private String getComune() {
            return comune;
        }

        private void setComune(String comune) {
            this.comune = comune;
        }

        private String getProvincia() {
            return provincia;
        }

        private void setProvincia(String provincia) {
            this.provincia = provincia;
        }

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Destination)) return false;
            Destination that = (Destination) o;
            return Objects.equals(getComune(), that.getComune()) && Objects.equals(getProvincia(), that.getProvincia()) && Objects.equals(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getComune(), getProvincia(), getName());
        }

        @NotNull
        @Override
        public String toString() {
            return "Destination{" +
                    "comune='" + comune + '\'' +
                    ", provincia='" + provincia + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
    
    private static class LineReportCheckConstants {
        private final static int DESTINATION_ALREADY_PRESENT = -1;
        private final static int DISAMBIGUATE_ORIGIN = 0;
        private final static int DISAMBIGUATE_DESTINATION = 1;
        private final static int CHECK_OKAY = 2;
    }
}
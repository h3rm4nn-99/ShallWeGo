package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout provinciaLayout, comuneLayout;
    private AutoCompleteTextView editTextProvincia, editTextComune;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        populateProvinciaDropDown();
    }

    public void populateProvinciaDropDown() {
        provinciaLayout = findViewById(R.id.provincia);
        comuneLayout = findViewById(R.id.comune);
        editTextProvincia = (AutoCompleteTextView) provinciaLayout.getEditText();
        editTextComune = (AutoCompleteTextView) comuneLayout.getEditText();
        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/province", response -> {
            JsonArray provinceJson = (JsonArray) JsonParser.parseString(response);
            Iterator<JsonElement> iterator = provinceJson.iterator();
            ArrayList<String> province = new ArrayList<>();
            while (iterator.hasNext()) {
                province.add(iterator.next().toString().replace("\"", ""));
            }
            Collections.sort(province);
            editTextProvincia.setAdapter(new ArrayAdapter<String>(RegisterActivity.this, R.layout.provincia_listitem_layout, province));
            dialog.dismiss();
        }, error -> {
            Toast.makeText(RegisterActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogProvince = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RegisterActivity.this.finish();
                        }
                    }).show();
            alertDialogProvince.setCancelable(false);
            alertDialogProvince.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        });

        q.add(request);
        editTextProvincia.setOnItemClickListener((parent, view, position, id) -> {
            ProgressDialog dialogComuni = ProgressDialog.show(RegisterActivity.this, "",
                    "Attendere prego...", true);
            String provincia = (String) parent.getItemAtPosition(position);
            editTextComune.setText("");
            RequestQueue q1 = Volley.newRequestQueue(getApplicationContext());
            StringRequest request1 = new StringRequest(Request.Method.GET, IpAddress.SERVER_IP_ADDRESS + "/api/provincia/" + provincia + "/comuni", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JsonArray comuniJson = (JsonArray) JsonParser.parseString(response);
                    Iterator<JsonElement> iterator = comuniJson.iterator();
                    ArrayList<String> comuni = new ArrayList<>();
                    while (iterator.hasNext()) {
                        comuni.add(iterator.next().toString().replace("\"", ""));
                    }
                    Collections.sort(comuni);
                    editTextComune.setAdapter(new ArrayAdapter<String>(RegisterActivity.this, R.layout.provincia_listitem_layout, comuni));
                    dialogComuni.dismiss();

                }
            }, error -> {
                Toast.makeText(RegisterActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                AlertDialog alertDialogComuni = new AlertDialog.Builder(RegisterActivity.this)
                        .setMessage("Controlla la tua connessione ad Internet e riprova!")
                        .setPositiveButton("Ho capito!", (dialog1, which) -> RegisterActivity.this.finish()).show();
                alertDialogComuni.setCancelable(false);
                alertDialogComuni.setCanceledOnTouchOutside(false);
                dialogComuni.dismiss();

            });
            q1.add(request1);

        });

    }

    public void proceed(View view) {
        boolean check = false;
        boolean passwordPresent = false;
        TextInputLayout username = findViewById(R.id.username);
        TextInputLayout password = findViewById(R.id.password);
        TextInputLayout passwordConfirm = findViewById(R.id.passwordConfirm);
        EditText usernameEditText = username.getEditText();
        EditText passwordEditText = password.getEditText();
        EditText passwordConfirmEditText = passwordConfirm.getEditText();
        username.setError(null);
        password.setError(null);
        passwordConfirm.setError(null);
        provinciaLayout.setError(null);
        comuneLayout.setError(null);

        if (usernameEditText.getText().toString().isEmpty()) {
            check = true;
            username.setError("Devi scegliere uno username!");
        }

        if (passwordEditText.getText().toString().isEmpty()) {
            check = true;
            password.setError("Devi scegliere una password!");
        }

        if (passwordConfirmEditText.getText().toString().isEmpty()) {
            check = true;
            passwordConfirm.setError("Digita di nuovo la tua password per confermarla!");
        } else {
            passwordPresent = true;
        }

        if (!passwordEditText.getText().toString().equals(passwordConfirmEditText.getText().toString()) && passwordPresent) {
            passwordConfirm.setError("Le due password non corrispondono!");
            check = true;
        }

        if (editTextProvincia.getText().toString().isEmpty()) {
            provinciaLayout.setError("Seleziona una provincia!");
            check = true;
        }

        if (editTextComune.getText().toString().isEmpty()) {
            comuneLayout.setError("Seleziona un comune!");
            check = true;
        }

        if (check)  return;

        ProgressDialog dialogRegistrazione = ProgressDialog.show(this, "",
                "Attendere prego...", true);

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        String requestBody = "username=" + usernameEditText.getText().toString() + "&password=" + passwordEditText.getText().toString() + "&comune=" + editTextComune.getText().toString() + "&provincia=" + editTextProvincia.getText().toString();
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.1.3:8080/api/register", response -> {
            if (response.equals("ERR_USER_ALREADY_PRESENT")) {
                dialogRegistrazione.dismiss();
                username.setError("Lo username digitato è già presente all'interno del sistema. Scegline un altro!");
            } else if (response.equals("OK")) {
                dialogRegistrazione.dismiss();
                AlertDialog alertDialogRegistrazione = new AlertDialog.Builder(RegisterActivity.this)
                        .setMessage("La registrazione è andata a buon fine. Ti stiamo reindirizzando alla pagina di login!")
                        .setPositiveButton("Ho capito!", (dialog, which) -> {
                            Intent i = new Intent(this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("username", usernameEditText.getText().toString());
                            i.putExtra("password", passwordEditText.getText().toString());
                            startActivity(i);
                            RegisterActivity.this.finish();
                        }).show();
                alertDialogRegistrazione.setCancelable(false);
                alertDialogRegistrazione.setCanceledOnTouchOutside(false);

            } else {
                dialogRegistrazione.dismiss();
                AlertDialog alertDialogRegistrazione = new AlertDialog.Builder(RegisterActivity.this)
                        .setMessage("Il server ha risposto inaspettatamente!")
                        .setPositiveButton("Ho capito!", (dialog, which) -> {}).show();
                alertDialogRegistrazione.setCancelable(false);
                alertDialogRegistrazione.setCanceledOnTouchOutside(false);
            }
        }, error -> {
            dialogRegistrazione.dismiss();
            AlertDialog alertDialogRegistrazione = new AlertDialog.Builder(RegisterActivity.this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", (dialog, which) -> {}).show();
            alertDialogRegistrazione.setCancelable(false);
            alertDialogRegistrazione.setCanceledOnTouchOutside(false);
        }) {
            @Override
            public byte[] getBody() {
                return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        q.add(request);

    }
}
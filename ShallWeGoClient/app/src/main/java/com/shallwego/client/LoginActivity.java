package com.shallwego.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout username, password;
    private EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        usernameEditText = username.getEditText();
        passwordEditText = password.getEditText();

        Intent i = getIntent();
        String incomingUsername = i.getStringExtra("username");
        String incomingPassword = i.getStringExtra("password");
        if (incomingUsername != null && incomingPassword != null) {
            usernameEditText.setText(incomingUsername);
            passwordEditText.setText(incomingPassword);
        }
    }

    public void login(View view) {
        username.setError(null);
        password.setError(null);
        boolean check = false;
        if (usernameEditText.getText().toString().isEmpty()) {
            username.setError("Il Nome Utente non può essere vuoto!");
            check = true;
        }
        if (passwordEditText.getText().toString().isEmpty()) {
            password.setError("La Password non può essere vuota!");
            check = true;
        }

        if (check)  return;

        ProgressDialog dialog = ProgressDialog.show(this, "",
                "Attendere prego...", true);
        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        String requestBody = "username=" + usernameEditText.getText().toString() + "&password=" + passwordEditText.getText().toString();
        StringRequest request = new StringRequest(Request.Method.POST,  IpAddress.SERVER_IP_ADDRESS + "/api/login", response -> {
            username.setError(null);
            password.setError(null);
            if (response.equals("ERR_USER_NOT_FOUND")) {
                dialog.dismiss();
                username.setError("Lo username digitato non è stato trovato!");
            } else if (response.equals("ERR_PWD_INCORRECT")) {
                dialog.dismiss();
                password.setError("La password digitata non corrisponde a questo nome utente!");
            } else {
                JsonObject user = (JsonObject) JsonParser.parseString(response);
                SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("user", usernameEditText.getText().toString());
                editor.putString("karma", user.get("karma").toString());
                editor.commit();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                dialog.dismiss();
                startActivity(i);
                LoginActivity.this.finish();
            }
        }, error -> {
            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            AlertDialog alertDialogProvince = new AlertDialog.Builder(this)
                    .setMessage("Controlla la tua connessione ad Internet e riprova!")
                    .setPositiveButton("Ho capito!", null).show();
            alertDialogProvince.setCancelable(false);
            alertDialogProvince.setCanceledOnTouchOutside(false);
            dialog.dismiss();
        }) {
            @Override
            public byte[] getBody() {
                return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        q.add(request);
    }


    public void register(View view) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }
}

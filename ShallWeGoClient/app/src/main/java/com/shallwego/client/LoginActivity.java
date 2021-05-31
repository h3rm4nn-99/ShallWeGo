package com.shallwego.client;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        String requestBody = "username=" + usernameEditText.getText().toString() + "&password=" + passwordEditText.getText().toString();
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.1.3:8080/api/login", response -> {
            if (response.equals("ERR_USER_NOT_FOUND")) {
                username.setError("Lo username digitato non è stato trovato!");
            } else if (response.equals("ERR_PWD_INCORRECT")) {
                password.setError("La password digitata non corrisponde a questo nome utente!");
            } else {
                Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(LoginActivity.this, "Errore!", Toast.LENGTH_SHORT).show()) {
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

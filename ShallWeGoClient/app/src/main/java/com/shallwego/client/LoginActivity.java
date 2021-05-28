package com.shallwego.client;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        TextInputLayout username = findViewById(R.id.username);
        TextInputLayout password = findViewById(R.id.password);
        EditText usernameEditText = username.getEditText();
        EditText passwordEditText = password.getEditText();
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
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.1.3:8080/api/login", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("ERR_USER_NOT_FOUND")) {
                    username.setError("Lo username digitato non è stato trovato!");
                } else if (response.equals("ERR_PWD_INCORRECT")) {
                    password.setError("La password digitata non corrisponde a questo nome utente!");
                } else {
                    Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "Errore!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        q.add(request);

    }
}

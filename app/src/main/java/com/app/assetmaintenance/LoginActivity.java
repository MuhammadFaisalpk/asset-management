package com.app.assetmaintenance;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.assetmaintenance.utils.CommonMethods;
import com.app.assetmaintenance.utils.LoadingDialog;
import com.app.assetmaintenance.utils.SharedPrefClass;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private String login_endpoint = "login";

    private EditText et_email, et_password;
    private Button btn_login;
    private TextView tv_signup;
    private LoadingDialog loadingDialog;
    private SharedPrefClass sharedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(view -> {
            LoginUser();
        });
        tv_signup = findViewById(R.id.tv_signup);
        tv_signup.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        sharedClass = new SharedPrefClass(LoginActivity.this);
        loadingDialog = new LoadingDialog(LoginActivity.this);
        loadingDialog.SetFullWidth();
    }

    private void LoginUser() {
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();

        loadingDialog.SetTitle("Please wait...");
        loadingDialog.Cancelable(false);
        loadingDialog.Show();


        if (!validateForm(email, password)) {
            return;
        }

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String URL = getString(R.string.base_url) + login_endpoint;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingDialog.Dismiss();

                        try {
                            boolean success = response.getBoolean("success");
                            String message = response.getString("message");
                            String token = response.getString("token");
                            if (success) {
                                JSONObject jsonObject1 = response.getJSONObject("data");
                                int id = jsonObject1.getInt("id");
                                int userType = jsonObject1.getInt("user_type");
                                sharedClass.setInt("user_id", id);
                                sharedClass.setString("user_token", token);

                                Intent intent;
                                if (userType == 0) {
                                    intent = new Intent(LoginActivity.this, AdminActivity.class);
                                } else {
                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                }
                                finish();
                                startActivity(intent);
                            } else {

                                String error = response.getString("error");

                                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception exception) {
                            exception.printStackTrace();
                            Toast.makeText(LoginActivity.this,
                                    "Some error occurred! Try again later.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                error -> {
                    loadingDialog.Dismiss();
                    CommonMethods.volleyErrors(error, LoginActivity.this);
                }) {
        };
        int socketTimeout = 30000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);

    }

    private boolean validateForm(String email, String password) {
        boolean valid = true;

        if (!email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("Enter Valid Email");
            valid = false;
        }
        if (!password.isEmpty()) {
            et_password.setError("Enter Valid Password");
            valid = false;
        }
        return valid;
    }
}
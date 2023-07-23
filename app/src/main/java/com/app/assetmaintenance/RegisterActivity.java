package com.app.assetmaintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.assetmaintenance.utils.CommonMethods;
import com.app.assetmaintenance.utils.LoadingDialog;
import com.app.assetmaintenance.utils.SharedPrefClass;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private String register_endpoint = "register";

    private EditText et_name, et_email, et_phone, et_department,
            et_designation, et_password, et_cpassword;
    private Button btn_register;
    private LoadingDialog loadingDialog;
    private SharedPrefClass sharedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
    }

    private void initView() {
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        et_phone = findViewById(R.id.et_phone);
        et_department = findViewById(R.id.et_department);
        et_designation = findViewById(R.id.et_designation);
        et_password = findViewById(R.id.et_password);
        et_cpassword = findViewById(R.id.et_cpassword);
        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(view -> {
            registerUser();
        });


        sharedClass = new SharedPrefClass(RegisterActivity.this);
        loadingDialog = new LoadingDialog(RegisterActivity.this);
        loadingDialog.SetFullWidth();
    }

    private void registerUser() {
        String name = et_name.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String phone = et_phone.getText().toString().trim();
        String department = et_department.getText().toString().trim();
        String designation = et_designation.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        String cpassword = et_cpassword.getText().toString().trim();

        loadingDialog.SetTitle("Please wait...");
        loadingDialog.Cancelable(false);
        loadingDialog.Show();


        if (!validateForm(name, email, phone, department,
                designation, password, cpassword)) {
            return;
        }

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", name);
            jsonObject.put("email", email);
            jsonObject.put("phone", phone);
            jsonObject.put("department", department);
            jsonObject.put("designation", designation);
            jsonObject.put("department", department);
            jsonObject.put("password", password);
            jsonObject.put("password_confirmation", cpassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String URL = getString(R.string.base_url) + register_endpoint;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonObject,
                response -> {
                    loadingDialog.Dismiss();

                    try {
                        boolean status = response.getBoolean("status");
                        String message = response.getString("message");
                        String token = response.getString("token");
                        if (status) {
                            JSONObject jsonObject1 = response.getJSONObject("data");
                            int id = jsonObject1.getInt("id");
                            sharedClass.setInt("user_id", id);
                            sharedClass.setString("user_token", token);

                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            finish();
                            startActivity(intent);
                        } else {

                            String error = response.getString("error");

                            Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Toast.makeText(RegisterActivity.this,
                                "Some error occurred! Try again later.",
                                Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    loadingDialog.Dismiss();
                    CommonMethods.volleyErrors(error, RegisterActivity.this);
                }) {
        };
        int socketTimeout = 30000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);
    }

    private boolean validateForm(String name, String email, String phone, String department,
                                 String designation, String password, String cpassword) {
        boolean valid = true;

        if (!name.isEmpty()) {
            et_name.setError("Enter name");
            valid = false;
        }
        if (!email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("Enter valid email");
            valid = false;
        }
        if (!phone.isEmpty()) {
            et_phone.setError("Enter phone number");
            valid = false;
        }
        if (!department.isEmpty()) {
            et_department.setError("Enter department");
            valid = false;
        }
        if (!designation.isEmpty()) {
            et_designation.setError("Enter designation");
            valid = false;
        }
        if (!password.isEmpty()) {
            et_password.setError("Enter Valid Password");
            valid = false;
        }
        if (!cpassword.isEmpty()) {
            et_cpassword.setError("Enter Valid Password");
            valid = false;
        }
        if (!password.equals(cpassword)) {
            et_cpassword.setError("Password doesn't match");
            valid = false;
        }
        return valid;
    }
}
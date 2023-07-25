package com.app.assetmaintenance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.assetmaintenance.user.UserMainActivity;
import com.app.assetmaintenance.utils.CommonMethods;
import com.app.assetmaintenance.utils.LoadingDialog;
import com.app.assetmaintenance.utils.SharedPrefClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private String register_endpoint = "register";
    private String department_endpoint = "getDepartment";

    private Spinner sp_department;
    private EditText et_name, et_email, et_phone,
            et_designation, et_password, et_cpassword;
    private Button btn_register;
    private LoadingDialog loadingDialog;
    private SharedPrefClass sharedClass;
    private ArrayList<String> departments = new ArrayList<>();
    private String sDepartment = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        getDepartments();
    }

    private void initView() {
        sp_department = findViewById(R.id.sp_department);
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        et_phone = findViewById(R.id.et_phone);
        et_designation = findViewById(R.id.et_designation);
        et_password = findViewById(R.id.et_password);
        et_cpassword = findViewById(R.id.et_cpassword);
        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(view -> {
            registerUser();
        });

        sp_department.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (departments.size() > 0) {
                    sDepartment = departments.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        sharedClass = new SharedPrefClass(RegisterActivity.this);
        loadingDialog = new LoadingDialog(RegisterActivity.this);
        loadingDialog.SetFullWidth();
    }

    private void getDepartments() {
        JSONObject jsonObject = new JSONObject();

        String URL = getString(R.string.base_url) + department_endpoint;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, jsonObject,
                response -> {
                    try {
                        boolean status = response.getBoolean("status");
                        if (status) {
                            JSONArray jsonArray = response.getJSONArray("data");
                            if (jsonArray.length() > 0) {
                                for (int j = 0; j < jsonArray.length(); j++) {

                                    JSONObject jsonObject1 = jsonArray.getJSONObject(j);

                                    String name = jsonObject1.getString("name");
                                    departments.add(name);
                                }
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_dropdown_item, departments);
                            sp_department.setAdapter(adapter);
                        }
                    } catch (Exception exception) {
                    }

                },
                error -> {
                    Log.e("TAG", "getDepartments: " + error.toString());
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

    private void registerUser() {
        String name = et_name.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String phone = et_phone.getText().toString().trim();
        String designation = et_designation.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        String cpassword = et_cpassword.getText().toString().trim();

        if (!validateForm(name, email, phone, sDepartment,
                designation, password, cpassword)) {
            return;
        }

        loadingDialog.SetTitle("Please wait...");
        loadingDialog.Cancelable(false);
        loadingDialog.Show();

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", name);
            jsonObject.put("email", email);
            jsonObject.put("phone", phone);
            jsonObject.put("department", sDepartment);
            jsonObject.put("designation", designation);
            jsonObject.put("password", password);
            jsonObject.put("password_confirmation", cpassword);
            jsonObject.put("user_type", 1);
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

                            Intent intent = new Intent(RegisterActivity.this, UserMainActivity.class);
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

        if (name.isEmpty()) {
            et_name.setError("Enter name");
            valid = false;
        }
        if (email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("Enter valid email");
            valid = false;
        }
        if (phone.isEmpty()) {
            et_phone.setError("Enter phone number");
            valid = false;
        }
        if (department.isEmpty()) {
            valid = false;
        }
        if (designation.isEmpty()) {
            et_designation.setError("Enter designation");
            valid = false;
        }
        if (password.isEmpty()) {
            et_password.setError("Enter Valid Password");
            valid = false;
        }
        if (cpassword.isEmpty()) {
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
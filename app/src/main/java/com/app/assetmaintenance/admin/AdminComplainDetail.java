package com.app.assetmaintenance.admin;

import static com.app.assetmaintenance.utils.CommonMethods.volleyErrors;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.assetmaintenance.R;
import com.app.assetmaintenance.models.ComplainsModel;
import com.app.assetmaintenance.models.UserModel;
import com.app.assetmaintenance.utils.LoadingDialog;
import com.app.assetmaintenance.utils.SharedPrefClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminComplainDetail extends AppCompatActivity {

    private String getAdmins_endpoint = "getAdmin";
    private String getComplains_endpoint = "getComplain";
    private int complain_id, assign_to;
    private EditText et_status;
    private Button btn_update;
    private TextView tv_desc, tv_assign_by, tv_assign_date;
    private Spinner sp_admin;
    private ArrayList<UserModel> adminsList = new ArrayList<>();
    private LoadingDialog loadingDialog;
    private SharedPrefClass sharedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complain_detail);

        initView();
        getComplainDetail();
        getAdmins();
    }

    private void initView() {
        sharedClass = new SharedPrefClass(AdminComplainDetail.this);
        loadingDialog = new LoadingDialog(AdminComplainDetail.this);
        loadingDialog.SetFullWidth();

        et_status = findViewById(R.id.et_status);
        btn_update = findViewById(R.id.btn_update);
        tv_desc = findViewById(R.id.tv_desc);
        tv_assign_by = findViewById(R.id.tv_assign_by);
        tv_assign_date = findViewById(R.id.tv_assign_date);
        sp_admin = findViewById(R.id.sp_admin);
        sp_admin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (adminsList.size() > 0) {
                    assign_to = adminsList.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        btn_update.setOnClickListener(view -> updateComplain());
    }

    private void updateComplain() {
    }

    private void getComplainDetail() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            complain_id = getIntent().getIntExtra("complain_id", 0);
        }

        JSONObject jsonObject = new JSONObject();

        String URL = getString(R.string.base_url) + getComplains_endpoint;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, jsonObject,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray data = response.getJSONArray("data");
                            if (data.length() != 0) {
                                for (int j = 0; j < data.length(); j++) {

                                    JSONObject jsonObject1 = data.getJSONObject(j);

                                    int id = jsonObject1.getInt("id");
                                    String description = jsonObject1.getString("description");
                                    String image = jsonObject1.getString("image");
                                    String status = jsonObject1.getString("status");
                                    String created_at = jsonObject1.getString("created_at");

                                    ComplainsModel complainsModel = new ComplainsModel();
                                    complainsModel.setId(id);
                                    complainsModel.setDescription(description);
                                    complainsModel.setImage(image);
                                    complainsModel.setStatus(status);
                                    complainsModel.setCreated_at(created_at);

                                }
                            }
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Toast.makeText(AdminComplainDetail.this, "Some error occurred! Try again later",
                                Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    volleyErrors(error, AdminComplainDetail.this);
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + sharedClass.getString("user_token"));

                return headers;
            }
        };
        int socketTimeout = 30000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(AdminComplainDetail.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);

    }

    private void getAdmins() {
        JSONObject jsonObject = new JSONObject();

        String URL = getString(R.string.base_url) + getAdmins_endpoint;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, jsonObject,
                response -> {
                    try {
                        boolean status = response.getBoolean("status");
                        if (status) {
                            ArrayList<String> userNames = new ArrayList<>();
                            JSONArray jsonArray = response.getJSONArray("data");
                            if (jsonArray.length() > 0) {
                                for (int j = 0; j < jsonArray.length(); j++) {

                                    JSONObject jsonObject1 = jsonArray.getJSONObject(j);

                                    int id = jsonObject1.getInt("id");
                                    String name = jsonObject1.getString("name");
                                    String email = jsonObject1.getString("email");
                                    String phone = jsonObject1.getString("phone");
                                    String department = jsonObject1.getString("department");
                                    String designation = jsonObject1.getString("designation");
                                    int userType = jsonObject1.getInt("user_type");

                                    userNames.add(name);
                                    adminsList.add(new UserModel(id, name, email, phone, department, designation, userType));
                                }
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                    android.R.layout.simple_spinner_dropdown_item, userNames);
                            sp_admin.setAdapter(adapter);
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

        RequestQueue requestQueue = Volley.newRequestQueue(AdminComplainDetail.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);
    }

}
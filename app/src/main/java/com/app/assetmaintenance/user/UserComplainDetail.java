package com.app.assetmaintenance.user;

import static com.app.assetmaintenance.utils.CommonMethods.volleyErrors;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.assetmaintenance.R;
import com.app.assetmaintenance.models.ComplainsModel;
import com.app.assetmaintenance.utils.LoadingDialog;
import com.app.assetmaintenance.utils.SharedPrefClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserComplainDetail extends AppCompatActivity {

    private String getComplains_endpoint = "getComplain";
    private int complain_id, assign_to;
    private TextView tv_desc, tv_status, tv_assign_by, tv_assign_date, tv_assign_to;
    private LoadingDialog loadingDialog;
    private SharedPrefClass sharedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_complain_detail);

        initView();
        getComplainDetail();
    }

    private void initView() {
        sharedClass = new SharedPrefClass(UserComplainDetail.this);
        loadingDialog = new LoadingDialog(UserComplainDetail.this);
        loadingDialog.SetFullWidth();

        tv_desc = findViewById(R.id.tv_desc);
        tv_status = findViewById(R.id.tv_status);
        tv_assign_by = findViewById(R.id.tv_assign_by);
        tv_assign_date = findViewById(R.id.tv_assign_date);
        tv_assign_to = findViewById(R.id.tv_assign_to);
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
                        Toast.makeText(UserComplainDetail.this, "Some error occurred! Try again later",
                                Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    volleyErrors(error, UserComplainDetail.this);
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

        RequestQueue requestQueue = Volley.newRequestQueue(UserComplainDetail.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);

    }
}
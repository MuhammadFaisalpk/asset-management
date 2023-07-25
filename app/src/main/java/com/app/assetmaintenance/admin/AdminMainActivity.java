package com.app.assetmaintenance.admin;

import static com.app.assetmaintenance.utils.CommonMethods.volleyErrors;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.assetmaintenance.R;
import com.app.assetmaintenance.adapters.ComplainsAdapter;
import com.app.assetmaintenance.models.ComplainsModel;
import com.app.assetmaintenance.utils.SharedPrefClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminMainActivity extends AppCompatActivity {

    private String getComplains_endpoint = "getComplain";
    private EditText et_search;
    private TextView tv_no_complains;
    private ProgressBar progressbar;
    private RecyclerView rv_all_complains;
    ComplainsAdapter complainsAdapter;
    ArrayList<ComplainsModel> complainsArray = new ArrayList<>();
    private SharedPrefClass sharedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        initView();
        getAllComplains();
    }

    private void initView() {
        sharedClass = new SharedPrefClass(AdminMainActivity.this);

        et_search = findViewById(R.id.et_search);
        tv_no_complains = findViewById(R.id.tv_no_complains);
        progressbar = findViewById(R.id.progressbar);
        rv_all_complains = findViewById(R.id.rv_all_complains);
        rv_all_complains.setLayoutManager(new LinearLayoutManager(AdminMainActivity.this,
                LinearLayoutManager.VERTICAL, false));

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (text.toString().length() > 0) {
                    ArrayList<ComplainsModel> listNew = new ArrayList<>();
                    for (int l = 0; l < complainsArray.size(); l++) {
                        String description = complainsArray.get(l).getDescription().toLowerCase();

                        if (description.contains(text.toString().toLowerCase())) {
                            listNew.add(complainsArray.get(l));
                        }
                    }
                    if (listNew.size() != 0) {
                        rv_all_complains.setVisibility(View.VISIBLE);
                        tv_no_complains.setVisibility(View.GONE);
                        progressbar.setVisibility(View.GONE);
                        complainsAdapter = new ComplainsAdapter(AdminMainActivity.this,
                                listNew, true);
                        rv_all_complains.setAdapter(complainsAdapter);
                    } else {
                        rv_all_complains.setVisibility(View.GONE);
                        tv_no_complains.setVisibility(View.VISIBLE);
                        progressbar.setVisibility(View.GONE);
                    }
                } else {
                    rv_all_complains.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.GONE);
                    complainsAdapter = new ComplainsAdapter(AdminMainActivity.this,
                            complainsArray, true);
                    rv_all_complains.setAdapter(complainsAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void getAllComplains() {
        JSONObject jsonObject = new JSONObject();

        String URL = getString(R.string.base_url) + getComplains_endpoint;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, jsonObject,
                response -> {
                    tv_no_complains.setVisibility(View.GONE);
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
                                    complainsArray.add(complainsModel);
                                }
                            }
                            rv_all_complains.setVisibility(View.VISIBLE);
                            tv_no_complains.setVisibility(View.GONE);
                            progressbar.setVisibility(View.GONE);
                            complainsAdapter = new ComplainsAdapter(AdminMainActivity.this,
                                    complainsArray, true);
                            rv_all_complains.setAdapter(complainsAdapter);
                        }
                    } catch (Exception exception) {
                        rv_all_complains.setVisibility(View.GONE);
                        tv_no_complains.setVisibility(View.VISIBLE);
                        progressbar.setVisibility(View.GONE);

                        exception.printStackTrace();
                        Toast.makeText(AdminMainActivity.this, "Some error occurred! Try again later",
                                Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    rv_all_complains.setVisibility(View.GONE);
                    tv_no_complains.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.GONE);

                    volleyErrors(error, AdminMainActivity.this);
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

        RequestQueue requestQueue = Volley.newRequestQueue(AdminMainActivity.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);

    }

    private void registerComplain() {
    }

}
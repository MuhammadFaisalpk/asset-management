package com.app.assetmaintenance.user;

import static com.app.assetmaintenance.utils.CommonMethods.volleyErrors;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.assetmaintenance.R;
import com.app.assetmaintenance.models.UserModel;
import com.app.assetmaintenance.utils.LoadingDialog;
import com.app.assetmaintenance.utils.SharedPrefClass;
import com.app.assetmaintenance.utils.VolleyMultipartRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterComplainActivity extends AppCompatActivity {

    private String registerComplain_endpoint = "complain";
    private String getAdmins_endpoint = "getAdmin";

    private RelativeLayout rl_photos;
    private ImageView iv_cover_photo;
    private Spinner sp_admin;
    private EditText et_desc;
    private Button btn_register;
    private LoadingDialog loadingDialog;
    private SharedPrefClass sharedClass;
    private ArrayList<UserModel> adminsList = new ArrayList<>();
    private int sAdmin = 0;
    public static final int PICK_IMAGE_CAMERA = 1, PERMISSIONS_CAMERA_REQUEST = 123;
    private File mCurrentPhotoFile;
    private Uri coverImage_URI;
    Bitmap Captured_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_complain);

        initView();
        getAdmins();
    }

    private void initView() {
        sharedClass = new SharedPrefClass(RegisterComplainActivity.this);
        loadingDialog = new LoadingDialog(RegisterComplainActivity.this);
        loadingDialog.SetFullWidth();

        rl_photos = findViewById(R.id.rl_photos);
        iv_cover_photo = findViewById(R.id.iv_cover_photo);
        et_desc = findViewById(R.id.et_desc);
        sp_admin = findViewById(R.id.sp_admin);
        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(view -> {
            registerComplain();
        });

        rl_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkPermission();
                } else {
                    openCamera();
                }
            }
        });


        sp_admin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (adminsList.size() > 0) {
                    sAdmin = adminsList.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
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

        RequestQueue requestQueue = Volley.newRequestQueue(RegisterComplainActivity.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(RegisterComplainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            openCamera();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_CAMERA_REQUEST);
        }
    }

    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        if (intent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.d("", "Couldn't create File");
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "app.pixarsart.stampbox.photo_editor_fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, PICK_IMAGE_CAMERA);
            }

        }
    }


    //function to create a file to store the image. It creates new file name with time stamp
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoFile = image;
        return image;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void setImageUri(ImageView cover_photo, Uri coverImage_uri) {
        Glide.with(RegisterComplainActivity.this)
                .load(coverImage_uri).into(cover_photo);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_CAMERA_REQUEST && grantResults.length > 0) {
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraPermission) {
                // write your logic here
                openCamera();
            } else {
                Toast.makeText(RegisterComplainActivity.this, "Permissions required.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CAMERA && resultCode == RESULT_OK) {
            try {
                String filePath = mCurrentPhotoFile.getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                Captured_bitmap = bitmap;
                coverImage_URI = getImageUri(RegisterComplainActivity.this, bitmap);

                setImageUri(iv_cover_photo, coverImage_URI);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registerComplain() {
        String description = et_desc.getText().toString().trim();

        if (!validateForm(description)) {
            return;
        }

        loadingDialog.SetTitle("Please wait...");
        loadingDialog.Cancelable(false);
        loadingDialog.Show();

        String URL = getString(R.string.base_url) + registerComplain_endpoint;

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL,
                response -> {
                    loadingDialog.Dismiss();

                    try {
                        JSONObject jsonObject1 = new JSONObject(new String(response.data));

                        boolean success = jsonObject1.getBoolean("success");
                        String message = jsonObject1.getString("message");
                        if (success) {

                            JSONObject result = jsonObject1.getJSONObject("result");

                            finish();

                        } else {
                            Toast.makeText(RegisterComplainActivity.this, "Some error occurred! Try again later.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Toast.makeText(RegisterComplainActivity.this, "Some error occurred! Try again later.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.Dismiss();

                        volleyErrors(error, RegisterComplainActivity.this);

                    }
                }) {
            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("description", description);
                params.put("status", "pending");
                params.put("user_id", String.valueOf(sharedClass.getInt("user_id")));
                params.put("user_assign_id", String.valueOf(sAdmin));
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long image_name = System.currentTimeMillis();

                DataPart dataPart = new DataPart(image_name + ".png", getFileDataFromDrawable(Captured_bitmap));
                params.put("image", dataPart);

                return params;
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("Authorization", "Bearer " + sharedClass.getString("user_token"));

                return params;
            }

        };
        int socketTimeout = 30000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        volleyMultipartRequest.setRetryPolicy(policy);
        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private boolean validateForm(String description) {
        boolean valid = true;

        if (description.isEmpty()) {
            et_desc.setError("Enter description");
            valid = false;
        }
        return valid;
    }
}
package com.app.assetmaintenance;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.app.assetmaintenance.user.UserMainActivity;
import com.app.assetmaintenance.utils.SharedPrefClass;

public class SplashActivity extends AppCompatActivity {

    SharedPrefClass sharedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initMain();
    }

    private void initMain() {
        sharedClass = new SharedPrefClass(SplashActivity.this);

        if (sharedClass.getInt("user_id") != 0) {
            Thread Splash = new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(3000);
                        }

                        startActivity(new Intent(SplashActivity.this, UserMainActivity.class));
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            Splash.start();
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    }
}


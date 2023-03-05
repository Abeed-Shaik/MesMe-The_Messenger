package com.abeed.mesme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.abeed.mesme.databinding.ActivitySplshScreenBinding;

import java.util.Objects;

public class SplshScreenActivity extends AppCompatActivity {

    ActivitySplshScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySplshScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        Thread thread = new Thread() {

            public void run(){
                try {
                    sleep(2100);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    Intent intent = new Intent(SplshScreenActivity.this, PhoneNumActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };thread.start();
    }
}
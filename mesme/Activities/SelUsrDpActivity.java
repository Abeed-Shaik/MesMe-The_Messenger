package com.abeed.mesme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.abeed.mesme.R;
import com.abeed.mesme.databinding.ActivitySelUsrDpBinding;
import com.bumptech.glide.Glide;

import java.util.Objects;

public class SelUsrDpActivity extends AppCompatActivity {

    ActivitySelUsrDpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_MesMe);
        binding = ActivitySelUsrDpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");
        Objects.requireNonNull(getSupportActionBar()).setTitle(name);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

        String profile = getIntent().getStringExtra("profile");
        Glide.with(SelUsrDpActivity.this)
                .load(profile)
                .placeholder(R.drawable.icuser)
                .into(binding.img1);

    }
}
package com.abeed.mesme.Activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.abeed.mesme.R;
import com.abeed.mesme.databinding.ActivityUsrDpBinding;
import com.bumptech.glide.Glide;

import java.util.Objects;

public class UsrDpActivity extends AppCompatActivity {

    ActivityUsrDpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_MesMe);
        binding = ActivityUsrDpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");
        Objects.requireNonNull(getSupportActionBar()).setTitle(name);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

        String profile = getIntent().getStringExtra("profile");
        Glide.with(UsrDpActivity.this)
                .load(profile)
                .placeholder(R.drawable.icuser)
                .into(binding.img2);

    }
}
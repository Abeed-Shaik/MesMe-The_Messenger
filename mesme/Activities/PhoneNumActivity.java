package com.abeed.mesme.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.abeed.mesme.R;
import com.abeed.mesme.databinding.ActivityPhoneNumBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class PhoneNumActivity extends AppCompatActivity {

    ActivityPhoneNumBinding binding;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.status_blue));
        binding = ActivityPhoneNumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0099FF")));

        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null) {
            Intent intent = new Intent(PhoneNumActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.phoneBox.requestFocus();

        binding.continueBtn.setOnClickListener(v -> {

            String ccode = binding.ccp.getSelectedCountryCodeWithPlus();
            String phno = binding.phoneBox.getText().toString();

            String phoneNumber = ccode+phno;
            String phneNmbr = ccode+" "+phno;

            if (phno.isEmpty()) {
                binding.phoneBox.setError("Enter your Phone number.");
                return;
            }

            new AlertDialog.Builder(PhoneNumActivity.this)
                    .setTitle(phneNmbr)
                    .setMessage("Is this OK, or would you like to edit the number?")
                    .setPositiveButton("OK", (dialog, which) -> {

                        Intent intent = new Intent(PhoneNumActivity.this, OTPActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        startActivity(intent);
                        dialog.dismiss();
                    }).setNegativeButton("EDIT", (dialog, which) -> dialog.dismiss()).show();

        });
    }
}
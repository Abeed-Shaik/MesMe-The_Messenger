package com.abeed.mesme.Activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.abeed.mesme.Models.User;
import com.abeed.mesme.R;
import com.abeed.mesme.databinding.ActivityDpImageBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class DpImageActivity extends AppCompatActivity {

    ActivityDpImageBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.dlt_dp, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.dltdp) {
            new AlertDialog.Builder(this)
                    .setTitle("REMOVE DP?")
                    .setPositiveButton("REMOVE", (dialog, which) -> {
                        String img = "https://firebasestorage.googleapis.com/v0/b/mesme-3.appspot.com/o/Profiles%2Fprofl.jpg?alt=media&token=5bb799fa-e270-4e71-b7c1-5132410a7686";
                        HashMap<String, Object> prf = new HashMap<>();
                        prf.put("profileImage", img);
                        database = FirebaseDatabase.getInstance();
                        String uid = FirebaseAuth.getInstance().getUid();
                        database.getReference().child("users").child(Objects.requireNonNull(uid)).updateChildren(prf)
                                        .addOnSuccessListener(unused -> {
                                            dialog.dismiss();
                                            Toast.makeText(DpImageActivity.this, "DP removed Successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                            startActivity(getIntent());
                                        }).addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(DpImageActivity.this, "Error in removing DP, please try again", Toast.LENGTH_SHORT).show();
                                        });
                    }).setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).show();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_MesMe);
        binding = ActivityDpImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile photo");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        database.getReference().child("users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Glide.with(DpImageActivity.this)
                                .load(Objects.requireNonNull(user).getProfileImage())
                                .placeholder(R.drawable.icuser)
                                .into(binding.dpimg);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
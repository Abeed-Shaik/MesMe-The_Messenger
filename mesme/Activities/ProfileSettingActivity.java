package com.abeed.mesme.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.abeed.mesme.Models.User;
import com.abeed.mesme.R;
import com.abeed.mesme.databinding.ActivityProfileSettingBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ProfileSettingActivity extends AppCompatActivity {

    ActivityProfileSettingBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.prof_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            new AlertDialog.Builder(this)
                    .setTitle("LOGOUT")
                    .setMessage("Do you really want to Logout?")
                    .setPositiveButton("LOGOUT", (dialog, which) -> {
                        auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        startActivity(new Intent(ProfileSettingActivity.this, PhoneNumActivity.class));
                        finish();
                        dialog.dismiss();
                    }).setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).show();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.status_blue));
        binding = ActivityProfileSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0099FF")));

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Profile...");
        dialog.setCancelable(false);

        binding.userName.setCursorVisible(false);
        binding.about.setCursorVisible(false);

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        database.getReference().child("users").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Glide.with(getApplicationContext())
                                .load(Objects.requireNonNull(user).getProfileImage())
                                .placeholder(R.drawable.icuser)
                                .into(binding.profile);
                        binding.userName.setText(user.getName());
                        binding.about.setText(user.getAbout());
                        binding.phnNum.setText(user.getPhoneNumber());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        binding.userName.setOnClickListener(v -> binding.userName.setCursorVisible(true));
        binding.about.setOnClickListener(v -> binding.about.setCursorVisible(true));

        binding.selPicBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            mselPhoto.launch(intent);
        });
        binding.profile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileSettingActivity.this, DpImageActivity.class);
            startActivity(intent);
        });
        binding.updtBtn.setOnClickListener(v -> {
            String name = binding.userName.getText().toString();
            String about = binding.about.getText().toString();
            if (name.isEmpty()) {
                binding.userName.setError("Username can't be Empty.");
                return;
            }
            if (about.isEmpty()) {
                binding.about.setError("About can't be Empty.");
                return;
            }
            dialog.show();
            if (selectedImage != null) {
                StorageReference reference = storage.getReference().child("Profiles").child(Objects.requireNonNull(auth.getUid()));
                reference.putFile(selectedImage).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImage = uri.toString();
                            HashMap<String, Object> profObj = new HashMap<>();
                            profObj.put("about", about);
                            profObj.put("name", name);
                            profObj.put("profileImage", profileImage);
                            database.getReference().child("users").child(uid).updateChildren(profObj).addOnSuccessListener(unused -> {
                                        dialog.dismiss();
                                        Toast.makeText(ProfileSettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                    }).addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(ProfileSettingActivity.this, "Error in updating Profile, please try again", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    }
                });
            } else {

                database.getReference().child("users").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                String profileImage = Objects.requireNonNull(user).getProfileImage();
                                Glide.with(getApplicationContext())
                                        .load(user.getProfileImage())
                                        .placeholder(R.drawable.icuser)
                                        .into(binding.profile);

                                HashMap<String, Object> profObj = new HashMap<>();
                                profObj.put("about", about);
                                profObj.put("name", name);
                                profObj.put("profileImage", profileImage);
                                database.getReference().child("users").child(uid).updateChildren(profObj).addOnSuccessListener(unused -> {
                                    dialog.dismiss();
                                    Toast.makeText(ProfileSettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    dialog.dismiss();
                                    Toast.makeText(ProfileSettingActivity.this, "Error in updating Profile, please try again", Toast.LENGTH_SHORT).show();
                                });

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
            binding.userName.setCursorVisible(false);
            binding.about.setCursorVisible(false);
        });
    }

    private final ActivityResultLauncher<Intent> mselPhoto = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getData() != null) {
                                Uri uri = data.getData();
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                long time = new Date().getTime();
                                StorageReference reference = storage.getReference().child("Profiles").child(time + "");
                                reference.putFile(uri);

                                binding.profile.setImageURI(data.getData());
                                selectedImage = data.getData();
                            }
                        }
                    }
                }
            });

}
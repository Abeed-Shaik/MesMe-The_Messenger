package com.abeed.mesme.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.abeed.mesme.Models.User;
import com.abeed.mesme.R;
import com.abeed.mesme.databinding.ActivitySetupProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.Objects;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.status_blue));
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0099FF")));

        dialog = new ProgressDialog(this);
        dialog.setMessage("Setting up Profile...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            mselPhoto.launch(intent);
        });
        binding.continueBtn.setOnClickListener(v -> {
            String name = binding.nameBox.getText().toString();
            String uid = auth.getUid();
            String phone = Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber();
            String abt = "Howdy! I am using Mesme.";
            Long msgs = 0L ;

            if (name.isEmpty()) {
                binding.nameBox.setError("Please type a name..");
                return;
            }

            dialog.show();
            if (selectedImage != null) {
                StorageReference reference = storage.getReference().child("Profiles").child(Objects.requireNonNull(auth.getUid()));
                reference.putFile(selectedImage).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            User user = new User(uid, name, phone, imageUrl, abt, msgs);
                            database.getReference()
                                    .child("users")
                                    .child(Objects.requireNonNull(uid))
                                    .setValue(user)
                                    .addOnSuccessListener(unused -> {
                                        dialog.dismiss();
                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }).addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(SetupProfileActivity.this, "Error in Setting up Profile!! Please try again.", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    }
                });
            } else {

                String prfimg = "https://firebasestorage.googleapis.com/v0/b/mesme-3.appspot.com/o/Profiles%2Fprofl.jpg?alt=media&token=5bb799fa-e270-4e71-b7c1-5132410a7686";
                User user = new User(uid, name, phone, prfimg, abt, msgs);
                database.getReference()
                        .child("users")
                        .child(Objects.requireNonNull(uid))
                        .setValue(user)
                        .addOnSuccessListener(unused -> {
                            dialog.dismiss();
                            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }).addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(SetupProfileActivity.this, "Error in Setting up Profile!! Please try again.", Toast.LENGTH_SHORT).show();
                        });
            }
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

                                binding.imageView.setImageURI(data.getData());
                                selectedImage = data.getData();
                            }
                        }
                    }
                }
            });
}